package ru.practicum.shareit.item;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Optional;

public interface ItemRepository {
    Item add(long idUser, Item item);

    Item patch(long idUser, long itemId, Item item);

    Optional<Item> getById(long itemId);

    List<Item> getAllByIdUser(long idUser);

    List<Item> search(String text);
}
