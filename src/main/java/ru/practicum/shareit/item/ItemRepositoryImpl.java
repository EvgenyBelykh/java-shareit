package ru.practicum.shareit.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.exceptions.ValidationNotFoundIdItemException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.exceptions.ValidationNotFoundIdUserException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ItemRepositoryImpl implements ItemRepository {
    private int id = 1;

    Map<Integer, Map<Integer, Item>> items = new HashMap<>();

    @Override
    public Item addItem(Integer idUser, Item item) {

        Map<Integer, Item> itemMap;
        if (items.get(idUser) == null) {
            itemMap = new HashMap<>();
        } else {
            itemMap = items.get(idUser);
        }
        item.setId(getId());
        itemMap.put(item.getId(), item);

        items.put(idUser, itemMap);
        log.info("Пользователь с id: {} добавил вещь:{}", idUser, item.getName());
        return item;
    }

    @Override
    public Item patchItem(Integer idUser, Integer itemId, Item item) {
        checkIdUserForSaveCertainItem(idUser, itemId, item);

        Map<Integer, Item> itemsOfUser = items.get(idUser);
        Item itemOfUser = itemsOfUser.get(itemId);

        if (item.getName() != null) {
            itemOfUser.setName(item.getName());
        }
        if (item.getDescription() != null) {
            itemOfUser.setDescription(item.getDescription());
        }
        if (item.getAvailable() != null) {
            itemOfUser.setAvailable(item.getAvailable());
        }

        itemsOfUser.put(itemId, itemOfUser);
        items.put(idUser, itemsOfUser);
        log.info("Пользователь с id: {} обновил вещь:{}", idUser, itemOfUser.getName());
        return itemOfUser;
    }

    @Override
    public Item getItemById(Integer itemId) {
        Item item = null;
        for (Map<Integer, Item> itemsOfCertainUser : items.values()) {
            if (itemsOfCertainUser.containsKey(itemId)) {
                item = itemsOfCertainUser.get(itemId);
            }
        }

        if (item == null) {
            throw new ValidationNotFoundIdItemException("Вещь с id: " + itemId + " не содержится в базе");
        }
        log.info("Возвращена вещь с id: {}", itemId);
        return item;
    }

    @Override
    public List<Item> getItemsByIdUser(Integer idUser) {
        checkIdUserForSaveItems(idUser);
        List<Item> itemsList = new ArrayList<>(items.get(idUser).values());
        log.info("Возвращен список вещей пользователя с id: {}", idUser);
        return itemsList;
    }

    @Override
    public List<Item> searchItem(String text) {
        String lowCaseText = text.toLowerCase();
        List<Item> itemsList = new ArrayList<>();
        if(text.isBlank()){
            log.info("Пустой запрос. Возвращен пустой список");
            return itemsList;
        }

        for (Map<Integer, Item> itemsOfCertainUser : items.values()) {
            for (Item item : itemsOfCertainUser.values()) {
                if ((item.getName().toLowerCase().contains(lowCaseText) ||
                        item.getDescription().toLowerCase().contains(lowCaseText)) &&
                        item.getAvailable()) {
                    itemsList.add(item);
                }
            }
        }

//        if(itemsList.size() == 0){
//            throw new ValidationNotFoundIdItemException("Не найдено ни одной вещи по запросу: " + text);
//        }

        log.info("Возвращен список доступных вещей по запросу: {}", text);
        return itemsList;
    }

    private void checkIdUserForSaveItems(Integer idUser) {
        if (!items.containsKey(idUser)) {
            throw new ValidationNotFoundIdUserException("У пользователя с id: " + idUser + "пока нет вещей для шеринга");
        }
    }

    private void checkIdUserForSaveCertainItem(Integer idUser, Integer itemId, Item item) {
        checkIdUserForSaveItems(idUser);

        if (!items.get(idUser).containsKey(itemId)) {
            throw new ValidationNotFoundIdUserException("Вещь: " + item.getName() +
                    " не принадлежит пользователю с id: " + idUser);
        }
    }

    private int getId() {
        return id++;
    }
}
