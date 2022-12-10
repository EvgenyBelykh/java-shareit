package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.ValidationItemDtoException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@Slf4j
public class ItemController {
    ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping
    public ItemDto addItemDto(@Valid @RequestBody ItemDto itemDto,
                              @RequestHeader(value = "X-Sharer-User-Id", required = false) Integer idUser) {
        validAvailableItemDto(itemDto);
        validDescriptionItemDto(itemDto);
        validNameItemDto(itemDto);
        validOwnerItemDtoNotNull(idUser);

        log.info("Запрос добавления вещи:{} от пользователя с id: {}", itemDto.getName(), idUser);

        return itemService.addItem(idUser, itemDto);
    }

    @PatchMapping("/{itemId}")
    public ItemDto patchItemDto(@RequestBody ItemDto itemDto,
                                @RequestHeader(value = "X-Sharer-User-Id", required = false) Integer idUser,
                                @PathVariable(value = "itemId") Integer itemId) {
        validOwnerItemDtoNotNull(idUser);
        checkPatchItemDto(itemDto);

        log.info("Запрос редактирования вещи: {} от пользователя с id: {}", itemDto.getName(), idUser);
        return itemService.patchItem(idUser, itemId, itemDto);
    }

    @GetMapping("{itemId}")
    public ItemDto getItemById(@PathVariable(value = "itemId") Integer itemId) {
        log.info("Запрос информации по вещи с id: {}", itemId);
        return itemService.getItemById(itemId);
    }

    @GetMapping
    public List<ItemDto> getItemsByIdUser(@RequestHeader(value = "X-Sharer-User-Id", required = false) Integer idUser) {
        validOwnerItemDtoNotNull(idUser);
        log.info("Запрос всех вещей пользователя с id: {}", idUser);
        return itemService.getItemsByIdUser(idUser);
    }

    @GetMapping("/search")
    public List<ItemDto> searchItem(@RequestParam(name = "text") String text) {
        log.info("Поиск свободных вещей по строке: {}", text);
        return itemService.searchItem(text);
    }

    private void checkPatchItemDto(ItemDto itemDto) {
        if ((itemDto.getName() == null || itemDto.getName().isEmpty() || itemDto.getName().isBlank()) &&
                (itemDto.getDescription() == null || itemDto.getDescription().isEmpty() || itemDto.getDescription().isBlank()) &&
                (itemDto.getAvailable() == null)
        ) {
            throw new ValidationItemDtoException("Не задано ни одно поле для обновления вещи");
        }
    }

    private void validAvailableItemDto(ItemDto itemDto) {
        if (itemDto.getAvailable() == null) {
            throw new ValidationItemDtoException("Не задана доступность вещи");
        }
    }

    private void validDescriptionItemDto(ItemDto itemDto) {
        if (itemDto.getDescription() == null) {
            throw new ValidationItemDtoException("Не задано описание вещи");
        }
        if (itemDto.getDescription().isEmpty() || itemDto.getDescription().isBlank()) {
            throw new ValidationItemDtoException("Описание вещи не может быть пустым или состоять только из пробелов");
        }
    }

    private void validNameItemDto(ItemDto itemDto) {
        if (itemDto.getName() == null) {
            throw new ValidationItemDtoException("Не задано имя добавляемой вещи");
        }
        if (itemDto.getName().isEmpty() || itemDto.getName().isBlank()) {
            throw new ValidationItemDtoException("Имя вещи не может быть пустым или состоять только из пробелов");
        }
    }

    private void validOwnerItemDtoNotNull(Integer idUser) {
        if (idUser == null) {
            throw new ValidationItemDtoException("Не задан владедец вещи");
        }
    }
}
