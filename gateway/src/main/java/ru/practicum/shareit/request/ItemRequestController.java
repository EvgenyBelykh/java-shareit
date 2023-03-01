package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.exceptions.IncorrectParameterException;
import ru.practicum.shareit.request.dto.ItemRequestDto;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemRequestController {
    private final ItemRequestClient itemRequestClient;

    @PostMapping
    public ResponseEntity<Object> addItemRequest(@Validated @RequestBody ItemRequestDto itemRequestDto,
                                                 @RequestHeader(value = "X-Sharer-User-Id") Long idUser) {

        log.info("Запрос добавления запроса вещи с описанием:{} от пользователя с id: {}",
                itemRequestDto.getDescription(), idUser);
        return itemRequestClient.addRequest(idUser, itemRequestDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllItemRequest(@RequestHeader(value = "X-Sharer-User-Id") Long idUser) {

        log.info("Запрос получения всех запросов вещей пользователя с id: {}", idUser);
        return itemRequestClient.getRequests(idUser);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> getByRequestId(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                                 @PathVariable(value = "requestId") Long requestId) {

        log.info("Запрос получения информации о запросе вещи с  id: {}", requestId);
        return itemRequestClient.getByRequestId(idUser, requestId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllByUserId(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                                 @RequestParam(value = "from", required = false) Integer from,
                                                 @RequestParam(value = "size", required = false) Integer size) {
        checkParameters(from, size);

        log.info("Запрос получения всех запросов вещей пользователем с id: {}", idUser);
        return itemRequestClient.getAllByIdUser(idUser, from, size);
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
