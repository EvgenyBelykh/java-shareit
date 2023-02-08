package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;
import ru.practicum.shareit.booking.dto.State;
import ru.practicum.shareit.booking.exceptions.WrongStateException;
import ru.practicum.shareit.item.exceptions.IncorrectParameterException;

import javax.validation.Valid;
import java.util.Objects;

@Controller
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Slf4j
@Validated
public class BookingController {
    private final BookingClient bookingClient;

    @GetMapping
    public ResponseEntity<Object> getAllByIdUser(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                                 @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                                 String stateString,
                                                 @RequestParam(value = "from", required = false) Integer from,
                                                 @RequestParam(value = "size", required = false) Integer size) {
        compareStateAndStringFromJson(stateString);
        checkParameters(from, size);

        log.info("Запрос получения списка всех бронирований со статусом: {} пользователя с id: {}",
                stateString, idUser);
        return bookingClient.getAllByIdUser(idUser, State.valueOf(stateString), from, size);
    }

    @GetMapping("/owner")
    public ResponseEntity<Object> getAllByIdOwner(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                                  @RequestParam(value = "state", defaultValue = "ALL", required = false)
                                                  String stateString,
                                                  @RequestParam(value = "from", required = false) Integer from,
                                                  @RequestParam(value = "size", required = false) Integer size) {
        compareStateAndStringFromJson(stateString);
        checkParameters(from, size);

        log.info("Запрос получения списка всех бронирований со статусом: {} вещей владельца с id: {}",
                stateString, idUser);
        return bookingClient.getAllByIdOwner(idUser, State.valueOf(stateString), from, size);
    }

    @PostMapping
    public ResponseEntity<Object> addBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                             @RequestBody @Valid BookItemRequestDto requestDto) {
        log.info("Creating booking {}, userId={}", requestDto, userId);
        return bookingClient.addBooking(userId, requestDto);
    }

    @PatchMapping("/{bookingId}")
    public ResponseEntity<Object> patchBooking(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                               @RequestParam(value = "approved") Boolean is_approved,
                                               @PathVariable(value = "bookingId") Long bookingId) {

        log.info("Запрос подтверждения/отклонения бронирования: {} пользователем с id:{} вещи с id: {}",
                is_approved, idUser, bookingId);
        return bookingClient.patchBooking(idUser, bookingId, is_approved);
    }

    @GetMapping("/{bookingId}")
    public ResponseEntity<Object> getByIdBooking(@RequestHeader("X-Sharer-User-Id") long userId,
                                                 @PathVariable Long bookingId) {
        log.info("Get booking {}, userId={}", bookingId, userId);
        return bookingClient.getByIdBooking(userId, bookingId);
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

    private void checkParameters(Integer from, Integer size) {
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
