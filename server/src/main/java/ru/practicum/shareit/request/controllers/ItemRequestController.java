package ru.practicum.shareit.request.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.exceptions.IncorrectParameterException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.services.ItemRequestService;

import java.util.List;

@RestController
@RequestMapping("/requests")
@Slf4j
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestService itemRequestService;

    @PostMapping
    public ItemRequestDto add(@Validated @RequestBody ItemRequestDto itemRequestDto,
                              @RequestHeader(value = "X-Sharer-User-Id") Long idUser) {

        log.info("Запрос добавления запроса вещи с описанием:{} от пользователя с id: {}",
                itemRequestDto.getDescription(), idUser);

        return itemRequestService.add(idUser, itemRequestDto);
    }

    @GetMapping
    public List<ItemRequestDto> getAll(@RequestHeader(value = "X-Sharer-User-Id") Long idUser) {

        log.info("Запрос получения всех запросов вещей пользователя с id: {}", idUser);

        return itemRequestService.getAll(idUser);
    }

    @GetMapping("/{requestId}")
    public ItemRequestDto getByRequestId(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                         @PathVariable(value = "requestId") Long requestId) {

        log.info("Запрос получения информации о запросе вещи с  id: {}", requestId);

        return itemRequestService.getByRequestId(idUser, requestId);
    }

    @GetMapping("/all")
    public List<ItemRequestDto> getAllByUserId(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                               @RequestParam(value = "from", required = false) Integer from,
                                               @RequestParam(value = "size", required = false) Integer size) {
        checkParameters(from, size);

        log.info("Запрос получения всех запросов вещей пользователем с id: {}", idUser);

        return itemRequestService.getAllByUserId(idUser, from, size);
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