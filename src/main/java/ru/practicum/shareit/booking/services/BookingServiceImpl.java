package ru.practicum.shareit.booking.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exceptions.*;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.item.repositries.ItemRepository;
import ru.practicum.shareit.item.exceptions.NoItemException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.services.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingServiceImpl implements BookingService {
    private final UserService userService;

    private final BookingRepository bookingRepository;
    private final ItemRepository itemRepository;

    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;

    @Override
    public BookingDto add(long idUser, AddBookingDto addBookingDto) {
        Item item = itemRepository.findById(addBookingDto.getItemId()).orElseThrow(() ->
                new NoItemException(addBookingDto.getItemId()));

        User owner = userMapper.toUser(userService.getById(item.getOwner().getId()));
        User booker = userMapper.toUser(userService.getById(idUser));

        if (idUser == owner.getId()) {
            throw new ValidationBelongsItemToUser("Пользователь не может взять вещь сам у себя в аренду");
        }

        if (!item.getAvailable()) {
            throw new ValidationBookingDtoException("Вещь с id: " + addBookingDto.getItemId() + " сейчас не доступна" +
                    " для бронирования");
        }

        checkBookingTime(addBookingDto);
        checkIntersectionsByBookingTime(addBookingDto);

        BookingDto bookingDto = new BookingDto();

        bookingDto.setStart(addBookingDto.getStart());
        bookingDto.setEnd(addBookingDto.getEnd());
        bookingDto.setItem(item);
        bookingDto.setBooker(booker);
        bookingDto.setStatus(Status.WAITING);

        Booking booking = bookingRepository.save(bookingMapper.toBooking(bookingDto));

        log.info("Сохранен запрос бронирования с id: {}", booking.getId());
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto patch(long bookingId, long idUser, Boolean is_approved) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NoBookingException(bookingId));

        Item item = booking.getItem();
        User owner = userMapper.toUser(userService.getById(item.getOwner().getId()));

        if (idUser != owner.getId()) {
            throw new ValidationBookingByOwnerItemOrBooker("Вещь с id= " + item.getId() + " не принадлежит пользователю " +
                    "с id= " + owner.getId());
        }

        if (booking.getStatus().equals(Status.APPROVED)) {
            throw new ValidationStatusException("Владелец вещи с id= " + owner.getId() + "уже подтвердил брованирование" +
                    " с id= " + bookingId);
        }

        if (booking.getStatus().equals(Status.REJECTED)) {
            throw new ValidationStatusException("Владелец вещи с id= " + owner.getId() + "уже отклонил брованирование" +
                    " с id= " + bookingId);
        }

        if (is_approved) {
            booking.setStatus(Status.APPROVED);
            log.info("Владелец вещи с id= {} подвердил бронирование с id={}", idUser, bookingId);
        } else {
            booking.setStatus(Status.REJECTED);
            log.info("Владелец вещи с id= {} отклонил бронирование с id={}", idUser, bookingId);
        }

        bookingRepository.save(booking);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public BookingDto getByIdBooking(long bookingId, long idUser) {
        Booking booking = bookingRepository.findById(bookingId).orElseThrow(() ->
                new NoBookingException(bookingId));

        if (idUser != booking.getBooker().getId() & idUser != booking.getItem().getOwner().getId()) {
            throw new ValidationBookingByOwnerItemOrBooker("Пользователь с id= " + idUser + " не является владельцем или" +
                    " арендатором вещи с id= " + booking.getItem().getId());
        }
        log.info("Возвращены данные о бронировани с id= {}", bookingId);
        return bookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> getAllByIdUser(long idUser, State state) {
        userService.isExistUser(idUser);

        List<Booking> bookingList = bookingRepository.findBookingsByIdUserAndSortTime(idUser);

        if (bookingList.isEmpty()) {
            throw new NoBookingBookerException(idUser);
        }

        List<Booking> curBookingList = getFilteredBookingListByState(bookingList, state);

        log.info("Возвращены все бронирования пользователя с id={} со статусом {}", idUser, state);
        return curBookingList.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> getAllByIdOwner(long idUser, State state) {
        userService.isExistUser(idUser);

        List<Booking> bookingList = bookingRepository.findBookingsByIdOwner(idUser);

        if (bookingList.isEmpty()) {
            throw new NoBookingOwnerException(idUser);
        }

        List<Booking> curBookingList = getFilteredBookingListByState(bookingList, state);

        log.info("Возвращены все бронирования вещей хозяина с id={} со статусом {}", idUser, state);
        return curBookingList.stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }

    private List<Booking> getFilteredBookingListByState(List<Booking> bookingList, State state) {
        switch (state) {
            case CURRENT:
                return bookingList.stream().filter(booking -> booking.getStart().isBefore(LocalDateTime.now()) &
                        booking.getEnd().isAfter(LocalDateTime.now())).collect(Collectors.toList());
            case PAST:
                return bookingList.stream().filter(booking -> booking.getEnd().isBefore(LocalDateTime.now())).
                        collect(Collectors.toList());
            case FUTURE:
                return bookingList.stream().filter(booking -> booking.getStart().isAfter(LocalDateTime.now())).
                        collect(Collectors.toList());
            case WAITING:
                return bookingList.stream().filter(booking -> booking.getStatus().equals(Status.WAITING)).
                        collect(Collectors.toList());
            case REJECTED:
                return bookingList.stream().filter(booking -> booking.getStatus().equals(Status.REJECTED)).
                        collect(Collectors.toList());
            default:
                return bookingList;
        }
    }

    private void checkIntersectionsByBookingTime(AddBookingDto addBookingDto) {
        List<BookingDto> bookingDtoList = getAllBooking();
        for (BookingDto bookingDto : bookingDtoList) {
            if (addBookingDto.getStart().isAfter(bookingDto.getStart()) &
                    addBookingDto.getStart().isBefore(bookingDto.getEnd()) &
                    addBookingDto.getItemId() == bookingDto.getItem().getId() &
                    bookingDto.getStatus().equals(Status.APPROVED)) {
                throw new ValidationBookingDtoException("Дата начала бронирования пересекается с уже" +
                        " подтвержденным бронированием для вещи с id = " + addBookingDto.getItemId());
            }

            if (addBookingDto.getEnd().isAfter(bookingDto.getStart()) &
                    addBookingDto.getEnd().isBefore(bookingDto.getEnd()) &
                    addBookingDto.getItemId() == bookingDto.getItem().getId() &
                    bookingDto.getStatus().equals(Status.APPROVED)) {
                throw new ValidationBookingDtoException("Дата окончания бронирования пересекается с уже" +
                        " подтвержденным бронированием для вещи с id = " + addBookingDto.getItemId());
            }

            if (addBookingDto.getStart().isBefore(bookingDto.getStart()) &
                    addBookingDto.getEnd().isAfter(bookingDto.getEnd()) &
                    addBookingDto.getItemId() == bookingDto.getItem().getId() &
                    bookingDto.getStatus().equals(Status.APPROVED)) {
                throw new ValidationBookingDtoException("На эти даты вещь c id = " + addBookingDto.getItemId() +
                        " уже забронирована другим пользователем");
            }
        }
    }

    private void checkBookingTime(AddBookingDto addBookingDto) {
        if (addBookingDto.getEnd().isBefore(addBookingDto.getStart())) {
            throw new ValidationBookingDtoException("Дата окончания бронирования раньше даты начала бронирования");
        }

        if (addBookingDto.getStart().isBefore(LocalDateTime.now())) {
            throw new ValidationBookingDtoException("Дата начала бронирования в прошлом");
        }

        if (addBookingDto.getEnd().isBefore(LocalDateTime.now())) {
            throw new ValidationBookingDtoException("Дата окончания бронирования в прошлом");
        }
    }

    private List<BookingDto> getAllBooking() {
        return bookingRepository.findAll().stream().map(bookingMapper::toBookingDto).collect(Collectors.toList());
    }
}
