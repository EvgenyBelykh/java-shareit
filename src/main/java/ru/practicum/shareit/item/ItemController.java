package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.Create;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.ValidationItemDtoException;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
@RequiredArgsConstructor
public class ItemController {
    private final ItemService itemService;

    @PostMapping
    public ItemDto add(@Validated(Create.class) @RequestBody ItemDto itemDto,
                       @RequestHeader(value = "X-Sharer-User-Id") Long idUser) {

        log.info("Запрос добавления вещи:{} от пользователя с id: {}", itemDto.getName(), idUser);

        return itemService.add(idUser, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patch(@RequestBody ItemDto itemDto,
                         @RequestHeader(value = "X-Sharer-User-Id") Long idUser,
                         @PathVariable(value = "itemId") Long itemId) {
        checkPatch(itemDto);

        log.info("Запрос редактирования вещи: {} от пользователя с id: {}", itemDto.getName(), idUser);
        return itemService.patch(idUser, itemId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemDto getById(@PathVariable(value = "itemId") Long itemId) {
        log.info("Запрос информации по вещи с id: {}", itemId);
        return itemService.getById(itemId);
    }

    @GetMapping
    public List<ItemDto> getAllByIdUser(@RequestHeader(value = "X-Sharer-User-Id") Long idUser) {
        log.info("Запрос всех вещей пользователя с id: {}", idUser);
        return itemService.getAllByIdUser(idUser);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam(name = "text") String text) {
        log.info("Поиск свободных вещей по строке: {}", text);
        if (text.isBlank()) {
            log.info("Пустой запрос. Возвращен пустой список");
            return Collections.emptyList();
        }
        return itemService.search(text);
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
