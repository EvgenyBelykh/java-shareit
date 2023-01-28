package ru.practicum.shareit.booking.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.exceptions.IncorrectParameterException;
import ru.practicum.shareit.booking.services.BookingService;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.exceptions.WrongStateException;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/bookings")
@Slf4j
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping
    public BookingDto add(@RequestBody AddBookingDto addBookingDto,
                          @RequestHeader(value = "X-Sharer-User-Id") Long idUser) {

        log.info("Запрос бронирования вещи с id: {}  пользователем с id: {}",
                addBookingDto.getItemId(), idUser);
        return bookingService.add(idUser, addBookingDto);
    }

    @PatchMapping("/{bookingId}")
    public BookingDto patch(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                            @RequestParam(value = "approved") Boolean is_approved,
                            @PathVariable(value = "bookingId") Long bookingId) {

        log.info("Запрос подтверждения/отклонения бронирования: {} пользователем с id:{} вещи с id: {}",
                is_approved, idUser, bookingId);
        return bookingService.patch(bookingId, idUser, is_approved);
    }

    @GetMapping("/{bookingId}")
    public BookingDto getByIdBooking(@PathVariable(value = "bookingId") Long bookingId,
                                     @RequestHeader(value = "X-Sharer-User-Id") Long idUser) {
        log.info("Запрос получения конкретных данных о бронировании с id: {} пользователем с id: {}",
                bookingId, idUser);
        return bookingService.getByIdBooking(bookingId, idUser);
    }

    @GetMapping
    public List<BookingDto> getAllByIdUser(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                           @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                           String stateString,
                                           @RequestParam(value = "from", required = false) Integer from,
                                           @RequestParam(value = "size", required = false) Integer size) {
        compareStateAndStringFromJson(stateString);
        checkParameters(from, size);

        log.info("Запрос получения списка всех бронирований со статусом: {} пользователя с id: {}",
                stateString, idUser);

        return bookingService.getAllByIdUser(idUser, State.valueOf(stateString), from, size);
    }

    @GetMapping("/owner")
    public List<BookingDto> getAllByIdOwner(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                            @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                            String stateString,
                                            @RequestParam(value = "from", required = false) Integer from,
                                            @RequestParam(value = "size", required = false) Integer size) {
        compareStateAndStringFromJson(stateString);
        checkParameters(from, size);

        log.info("Запрос получения списка всех бронирований со статусом: {} вещей владельца с id: {}",
                stateString, idUser);
        return bookingService.getAllByIdOwner(idUser, State.valueOf(stateString), from, size);
    }

    private void compareStateAndStringFromJson(String stateString) {
        if (!Objects.equals(stateString, State.ALL.toString()) &
                !Objects.equals(stateString, State.PAST.toString()) &
                !Objects.equals(stateString, State.CURRENT.toString()) &
                !Objects.equals(stateString, State.FUTURE.toString()) &
                !Objects.equals(stateString, State.WAITING.toString()) &
                !Objects.equals(stateString, State.REJECTED.toString())) {
            throw new WrongStateException("Unknown state: UNSUPPORTED_STATUS");
        }
    }
    private void checkParameters(Integer from, Integer size){
        if (from != null && from < 0) {
            log.info("Задан неправильный номер элемента для пагинации = {}", from);
            throw new IncorrectParameterException("from");
        }
        if (size != null && size <= 0) {
            log.info("Задан неправильный размер страницы для пагинации = {}", size);
            throw new IncorrectParameterException("size");
        }
        if (from != null & size == null |
                from == null & size != null) {
            log.info("Один из параметров пагинации null - from = {}, size={}", from, size);
            throw new IncorrectParameterException("size или from");
        }
    }

}
