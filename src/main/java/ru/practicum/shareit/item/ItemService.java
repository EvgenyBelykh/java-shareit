package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto addItem(Integer idUser, ItemDto itemDto);

    ItemDto patchItem(Integer idUser, Integer itemId, ItemDto itemDto);

    ItemDto getItemById(Integer itemId);

    List<ItemDto> getItemsByIdUser(Integer idUser);

    List<ItemDto> searchItem(String text);
}
