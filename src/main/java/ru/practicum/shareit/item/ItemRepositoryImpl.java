package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.exceptions.ValidationNotFoundIdUserException;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class ItemRepositoryImpl implements ItemRepository {
    private long id = 1;
    private final Map<Long, Item> storage = new LinkedHashMap<>();
    private final Map<Long, List<Item>> userItemIndex = new LinkedHashMap<>();

    @Override
    public Item add(long idUser, Item item) {

        final List<Item> items = userItemIndex.computeIfAbsent(item.getOwner().getId(), k -> new ArrayList<>());
        item.setId(getId());
        items.add(item);

        storage.put(item.getId(), item);
        log.info("Пользователь с id: {} добавил вещь:{}", idUser, item.getName());
        return item;
    }

    @Override
    public Item patch(long idUser, long itemId, Item item) {
        checkUserForSaveItems(idUser);
        checkUserForSaveCertainItem(idUser, itemId);

        final Item curItem = storage.get(itemId);

        if (item.getName() != null && !item.getName().isBlank()) {
            curItem.setName(item.getName());
        }
        if (item.getDescription() != null && !item.getDescription().isBlank()) {
            curItem.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            curItem.setAvailable(item.getAvailable());
        }

        log.info("Пользователь с id: {} обновил вещь:{}", idUser, curItem.getName());
        return curItem;
    }

    @Override
    public Optional<Item> getById(long itemId) {
        return Optional.ofNullable(storage.get(itemId));
    }

    @Override
    public List<Item> getAllByIdUser(long idUser) {
        checkUserForSaveItems(idUser);
        List<Item> items = userItemIndex.get(idUser);
        log.info("Возвращен список вещей пользователя с id: {}", idUser);
        return items;
    }

    @Override
    public List<Item> search(String text) {
        return storage.values().stream().filter(item ->
                item.getName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getDescription().toLowerCase().contains(text.toLowerCase()) &&
                                item.getAvailable())
                .collect(Collectors.toList());
    }

    private void checkUserForSaveItems(long idUser) {
        if (!userItemIndex.containsKey(idUser)) {
            throw new ValidationNotFoundIdUserException("У пользователя с id: " + idUser + "пока нет вещей для шеринга");
        }
    }

    private void checkUserForSaveCertainItem(long idUser, long itemId) {
        if (storage.get(itemId).getOwner().getId() != idUser) {
            throw new ValidationNotFoundIdUserException("Вещь c id: " + itemId +
                    " не принадлежит пользователю с id: " + idUser);
        }
    }

    private long getId() {
        return id++;
    }
}
