package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto add(long idUser, ItemDto itemDto);

    ItemDto patch(long idUser, long itemId, ItemDto itemDto);

    ItemDto getById(long itemId);

    List<ItemDto> getAllByIdUser(long idUser);

    List<ItemDto> search(String text);
}
