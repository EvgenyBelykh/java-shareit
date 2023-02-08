package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.item.dto.AddCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.EmptyCommentException;
import ru.practicum.shareit.item.exceptions.IncorrectParameterException;
import ru.practicum.shareit.item.exceptions.ValidationItemDtoException;

import java.util.Collections;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
    private final ItemClient itemClient;

    @PostMapping
    public ResponseEntity<Object> add(@Validated(Create.class) @RequestBody ItemDto itemDto,
                                      @RequestHeader(value = "X-Sharer-User-Id") Long idUser) {

        log.info("Запрос добавления вещи:{} от пользователя с id: {}", itemDto.getName(), idUser);
        return itemClient.addItem(idUser, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Object> patch(@RequestBody ItemDto itemDto,
                                        @RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                        @PathVariable(value = "itemId") Long itemId) {
        checkPatch(itemDto);

        log.info("Запрос редактирования вещи: {} от пользователя с id: {}", itemDto.getName(), idUser);
        return itemClient.patchItem(idUser, itemId, itemDto);
    }

    @GetMapping
    public ResponseEntity<Object> getAllByIdUser(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                                 @RequestParam(value = "from", required = false) Integer from,
                                                 @RequestParam(value = "size", required = false) Integer size) {
        checkParameters(from, size);
        log.info("Запрос всех вещей пользователя с id: {}", idUser);
        return itemClient.getAllByIdUser(idUser, from, size);
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<Object> getById(@PathVariable(value = "itemId") Long itemId,
                                          @RequestHeader(value = "X-Sharer-User-Id") Long idUser) {
        log.info("Запрос информации по вещи с id: {} пользователем с id={}", itemId, idUser);
        return itemClient.getByIdItem(itemId, idUser);
    }

    @GetMapping("/search")
    public ResponseEntity<Object> search(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                         @RequestParam(name = "text") String text,
                                         @RequestParam(value = "from", required = false) Integer from,
                                         @RequestParam(value = "size", required = false) Integer size) {
        log.info("Поиск свободных вещей по строке: {}", text);
        checkParameters(from, size);
        return itemClient.searchItem(idUser, text, from, size);
    }

    @PostMapping("/{itemId}/comment")
    public ResponseEntity<Object> addComment(@RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                                             @PathVariable(value = "itemId") Long itemId,
                                             @RequestBody AddCommentDto addCommentDto) {
        if (addCommentDto.getText().isBlank()) {
            throw new EmptyCommentException("Пустой запрос");
        }
        log.info("Запрос добавления отзыва от пользователя с id= {} для вещи с id: {}",
                idUser, itemId);
        return itemClient.addComment(idUser, itemId, addCommentDto);
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

    private void checkPatch(ItemDto itemDto) {
        if ((itemDto.getName() == null || itemDto.getName().isBlank()) &&
                (itemDto.getDescription() == null || itemDto.getDescription().isBlank()) &&
                (itemDto.getAvailable() == null)
        ) {
            throw new ValidationItemDtoException("Не задано ни одно поле для обновления вещи");
        }
    }

}
