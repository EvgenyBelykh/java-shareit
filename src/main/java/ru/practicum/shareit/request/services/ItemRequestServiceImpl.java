package ru.practicum.shareit.request.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemRepository;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.NoItemRequestException;
import ru.practicum.shareit.request.mappers.ItemRequestMapper;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.repositories.ItemRequestRepository;
import ru.practicum.shareit.user.exceptions.NoUserException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserRepository;
import ru.practicum.shareit.user.services.UserService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemRequestServiceImpl implements ItemRequestService {
    private final UserService userService;
    private final ItemRequestRepository itemRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ItemRequestMapper itemRequestMapper;
    private final ItemMapper itemMapper;

    private static final Sort SORT_CREATED_ASC = Sort.by(Sort.Direction.ASC, "created");

    @Override
    public ItemRequestDto add(Long idUser, ItemRequestDto itemRequestDto) {
        User user = userRepository.findById(idUser).orElseThrow(() -> new NoUserException(idUser));

        ItemRequest itemRequest = itemRequestRepository.save(itemRequestMapper.toItemRequest(itemRequestDto,
                LocalDateTime.now(), user));

        List<Item> items = itemRepository.findByItemRequestIdOrderById(itemRequest.getId());
        log.info("Сохранен запрос с id= {} вещи от пользователя с id: {}", itemRequest.getId(), idUser);

        return itemRequestMapper.toItemRequestDto(itemRequest, itemsToItemsDtoWithRequest(items));

    }

    @Override
    public List<ItemRequestDto> getAll(Long idUser) {
        if (!userService.isExistUser(idUser)) {
            throw new NoUserException(idUser);
        }

        List<ItemRequest> itemRequestList = itemRequestRepository.findItemRequestByUserIdOrderByCreatedAsc(idUser);

        List<ItemRequestDto> itemRequestDtoList = new ArrayList<>();
        for (ItemRequest itemRequest : itemRequestList) {
            itemRequestDtoList.add(itemRequestMapper.toItemRequestDto(itemRequest,
                    itemsToItemsDtoWithRequest(itemRepository.findByItemRequestIdOrderById(itemRequest.getId()))));
        }

        log.info("Возвращены все запросы вещей пользователя с id: {} с ответами на них", idUser);
        return itemRequestDtoList;
    }

    @Override
    public ItemRequestDto getByRequestId(Long idUser, Long requestId) {
        if (!userService.isExistUser(idUser)) {
            throw new NoUserException(idUser);
        }

        ItemRequest itemRequest = itemRequestRepository.findItemRequestById(requestId).orElseThrow(()
                -> new NoItemRequestException(requestId));

        List<Item> items = itemRepository.findByItemRequestIdOrderById(itemRequest.getId());

        log.info("Возвращен запрос вещи  с id: {}", requestId);
        return itemRequestMapper.toItemRequestDto(itemRequest, itemsToItemsDtoWithRequest(items));
    }

    @Override
    public List<ItemRequestDto> getAllByUserId(Long idUser, Integer from, Integer size) {
        if (!userService.isExistUser(idUser)) {
            throw new NoUserException(idUser);
        }
        if (size == null) {
            List<ItemRequest> itemRequestList = itemRequestRepository.findItemRequestByUserIdNot(idUser);

            List<ItemRequestDto> itemRequestDtoList = new ArrayList<>();

            for (ItemRequest itemRequest : itemRequestList) {
                itemRequestDtoList.add(itemRequestMapper.toItemRequestDto(itemRequest,
                        itemsToItemsDtoWithRequest(itemRepository.findByItemRequestIdOrderById(itemRequest.getId()))));
            }

            log.info("Возвращены все запросы вещей исключая пользователя с id={}", idUser);
            return itemRequestDtoList;

        } else {
            Pageable pageable = PageRequest.of(from / size, size, SORT_CREATED_ASC);

            Page<ItemRequest> itemRequestPage = itemRequestRepository.findItemRequestByUserIdNot(idUser, pageable);
            log.info("Возвращены все запросы вещей исключая пользователя с id={} с пагинацией from={}, size={}"
                    , idUser, from, size);
            return itemRequestPage.map(itemRequest -> {

                ItemRequestDto itemRequestDto = new ItemRequestDto();
                List<Item> items = itemRepository.findByItemRequestIdOrderById(itemRequest.getId());

                itemRequestDto.setId(itemRequest.getId());
                itemRequestDto.setDescription(itemRequest.getDescription());
                itemRequestDto.setCreated(itemRequest.getCreated());
                itemRequestDto.setItems(itemsToItemsDtoWithRequest(items));

                return itemRequestDto;
            }).getContent();
        }
    }

    private List<ItemDto> itemsToItemsDtoWithRequest(List<Item> items) {
        return items.stream().map(itemMapper::toItemDtoWithoutBookingWithRequest).collect(Collectors.toList());
    }
}
