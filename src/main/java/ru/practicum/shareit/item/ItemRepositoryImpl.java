package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.exceptions.ValidationNotFoundIdUserException;

import java.util.*;

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

        final List<Item> items = userItemIndex.get(idUser);
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

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == itemId) {
                items.set(i, curItem);
            }
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
        String lowCaseText = text.toLowerCase();
        List<Item> itemsList = new ArrayList<>();

        for (Item item : storage.values()) {
            if ((item.getName().toLowerCase().contains(lowCaseText) ||
                    item.getDescription().toLowerCase().contains(lowCaseText)) && item.getAvailable()) {
                itemsList.add(item);
            }
        }

        log.info("Возвращен список доступных вещей по запросу: {}", text);
        return itemsList;
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
