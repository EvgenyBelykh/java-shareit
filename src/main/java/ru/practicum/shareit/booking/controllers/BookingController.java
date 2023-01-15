package ru.practicum.shareit.booking.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.serviсes.BookingService;
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
                                           String stateString) {
        compareStateAndStringFromJson(stateString);

        log.info("Запрос получения списка всех бронирований со статусом: {} пользователя с id: {}",
                stateString, idUser);

        return bookingService.getAllByIdUser(idUser, State.valueOf(stateString));
    }

    @GetMapping("/owner")
    public List<BookingDto> getAlByIdOwner(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                           @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                           String stateString) {
        compareStateAndStringFromJson(stateString);

        log.info("Запрос получения списка всех бронирований со статусом: {} вещей владельца с id: {}",
                stateString, idUser);
        return bookingService.getAllByIdOwner(idUser, State.valueOf(stateString));
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


}
