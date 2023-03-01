package ru.practicum.shareit.item.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.item.dto.AddCommentDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.exceptions.CommentFutureException;
import ru.practicum.shareit.item.exceptions.NoBookingCommentException;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.repositories.CommentRepository;
import ru.practicum.shareit.item.repositories.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.NoItemException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.LastBooking;
import ru.practicum.shareit.item.model.NextBooking;
import ru.practicum.shareit.request.exception.NoItemRequestException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositories.ItemRequestRepository;
import ru.practicum.shareit.user.services.UserService;
import ru.practicum.shareit.user.exceptions.ValidationNotFoundIdUserException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;
    private final CommentMapper commentMapper;
    private static final Sort SORT_ID_ASC = Sort.by(Sort.Direction.ASC, "id");

    @Override
    public ItemDto add(long idUser, ItemDto itemDto) {
        User user = userMapper.toUser(userService.getById(idUser));

        if (itemDto.getRequestId() == null) {
            Item item = itemRepository.save(itemMapper.toItem(itemDto, user));
            log.info("Сохранена вещь с id: {} пользователя с id: {}", item.getId(), idUser);
            return itemMapper.toItemDtoWithoutBooking(item);
        } else {
            ItemRequest itemRequest = itemRequestRepository.findItemRequestById(itemDto.getRequestId()).orElseThrow(()
                    -> new NoItemRequestException(itemDto.getRequestId()));

            Item item = itemRepository.save(itemMapper.toItemWithRequest(itemDto, user, itemRequest));
            return itemMapper.toItemDtoWithoutBookingWithRequest(item);
        }

    }

    @Override
    public ItemDto patch(long idUser, long itemId, ItemDto itemDto) {
        Item curItem = itemRepository.findById(itemId).orElseThrow(() -> new NoItemException(itemId));
        checkUserForSaveItems(idUser);
        checkUserForSaveCertainItem(idUser, itemId);
        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            curItem.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            curItem.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            curItem.setAvailable(itemDto.getAvailable());
        }
        Item item = itemRepository.save(curItem);
        log.info("Обновлена вещь с id: {} пользователя с id: {}", item.getId(), idUser);
        return itemMapper.toItemDtoWithoutBooking(item);
    }

    @Override
    public ItemDto getById(long itemId, long idUser) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NoItemException(itemId));
        List<CommentDto> comments = commentsToCommentsDto(commentRepository.findCommentsByItemId(itemId));

        if (item.getOwner().getId() == idUser) {
            Booking bookingNext = bookingRepository.findFirstBookingByItemIdAndStartAfterOrderByStartAsc(itemId,
                    LocalDateTime.now());
            Booking bookingLast = bookingRepository.findTopBookingByItemIdOrderByStartAsc(itemId);

            if (bookingNext == null & bookingLast == null) {
                log.info("Возвращена вещь с id: {}", itemId);
                return itemMapper.toItemDtoWithoutBookingWithComments(item, comments);
            }

            LastBooking lastBooking = null;
            NextBooking nextBooking = null;
            if (bookingLast != null) {
                lastBooking = new LastBooking(bookingLast.getId(), bookingLast.getBooker().getId());
            }
            if (bookingNext != null) {
                nextBooking = new NextBooking(bookingNext.getId(), bookingNext.getBooker().getId());
            }

            log.info("Возвращена вещь с id: {} и следующими бронированиями с id={} и {}", itemId,
                    lastBooking != null ? lastBooking.getId() : null,
                    nextBooking != null ? nextBooking.getId() : null);
            return itemMapper.toItemDtoWithBookingWithComments(item, lastBooking, nextBooking, comments);

        } else {
            log.info("Возвращена вещь с id: {}", itemId);
            return itemMapper.toItemDtoWithoutBookingWithComments(item, comments);
        }
    }

    @Override
    public List<ItemDto> getAllByIdUser(long idUser, Integer from, Integer size) {
        userService.isExistUser(idUser);
        checkUserForSaveItems(idUser);
        List<ItemDto> itemDtoList;

        if (size == null) {
            itemDtoList = itemRepository.findByOwnerIdOrderById(idUser).stream()
                    .map(itemMapper::toItemDtoWithoutBooking).collect(Collectors.toList());
        } else {
            Pageable pageable = PageRequest.of(from / size, size, SORT_ID_ASC);

            itemDtoList = itemRepository.findByOwnerIdOrderById(idUser, pageable).getContent()
                    .stream().map(itemMapper::toItemDtoWithoutBooking)
                    .collect(Collectors.toList());
        }

        for (ItemDto itemDto : itemDtoList) {
            Booking bookingNext = bookingRepository.findFirstBookingByItemIdAndStartAfterOrderByStartAsc(itemDto.getId(),
                    LocalDateTime.now());
            Booking bookingLast = bookingRepository.findTopBookingByItemIdOrderByStartAsc(itemDto.getId());
            itemDto.setComments(commentsToCommentsDto(commentRepository.findCommentsByItemId(itemDto.getId())));

            if (bookingNext != null) {
                NextBooking nextBooking = new NextBooking(bookingNext.getId(), bookingNext.getBooker().getId());
                itemDto.setNextBooking(nextBooking);
            }

            if (bookingLast != null) {
                LastBooking lastBooking = new LastBooking(bookingLast.getId(), bookingLast.getBooker().getId());
                itemDto.setLastBooking(lastBooking);
            }
        }

        log.info("Возвращены все вещи пользователя с id= {}", idUser);
        return itemDtoList;
    }

    @Override
    public List<ItemDto> search(String text, Integer from, Integer size) {
        List<ItemDto> searchList;
        if (size == null) {
            searchList = itemsToItemsDto(itemRepository.search(text.toLowerCase()));
        } else {
            Pageable pageable = PageRequest.of(from / size, size, SORT_ID_ASC);
            searchList = itemsToItemsDto(itemRepository.search(text, pageable).getContent());
        }
        log.info("Возвращен список доступных вещей по запросу: {}", text);
        return searchList;
    }

    @Override
    public CommentDto addComment(long idUser, long itemId, AddCommentDto addCommentDto) {
        User author = userMapper.toUser(userService.getById(idUser));
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new NoItemException(itemId));
        Booking booking = bookingRepository.findFirstBookingByItemIdAndBookerIdAndStatusOrderByStartAsc(itemId, idUser,
                Status.APPROVED).orElseThrow(() ->
                new NoBookingCommentException("Пользователь еще не арендовал эту вещь"));

        if (booking.getStart().isAfter(LocalDateTime.now())) {
            throw new CommentFutureException("Нельзя делать отзыв к еще не взятой в аренду вещи");
        }

        Comment comment = commentRepository.save(commentMapper.toComment(addCommentDto, author, item,
                LocalDateTime.now()));
        log.info("Сохранен отзыв для вещи с id= {} от пользователя с id={}", itemId, idUser);

        return commentMapper.toCommentDto(comment);
    }

    private List<ItemDto> itemsToItemsDto(List<Item> items) {
        return items.stream().map(itemMapper::toItemDtoWithoutBooking).collect(Collectors.toList());
    }

    private List<CommentDto> commentsToCommentsDto(List<Comment> comments) {
        return comments.stream().map(commentMapper::toCommentDto).collect(Collectors.toList());
    }

    private void checkUserForSaveItems(long idUser) {
        if (itemRepository.findByOwnerIdOrderById(idUser).isEmpty()) {
            throw new ValidationNotFoundIdUserException("У пользователя с id: " + idUser + " пока нет вещей для шеринга");
        }
    }

    private void checkUserForSaveCertainItem(long idUser, long itemId) {
        if (itemRepository.findOwnerByIdItem(itemId) != idUser) {
            throw new ValidationNotFoundIdUserException("Вещь c id: " + itemId +
                    " не принадлежит пользователю с id: " + idUser);
        }
    }
}
