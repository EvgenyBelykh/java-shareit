package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    Item addItem(Integer idUser, Item item);

    Item patchItem(Integer idUser, Integer itemId, Item item);

    Item getItemById(Integer itemId);

    List<Item> getItemsByIdUser(Integer idUser);

    List<Item> searchItem(String text);
}
