package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier(value = "itemRepositoryImpl")
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper = new ItemMapper();

    @Override
    public ItemDto addItem(Integer idUser, ItemDto itemDto) {
        userService.checkUserById(idUser);
        Item item = itemRepository.addItem(idUser, itemMapper.toItem(itemDto));
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto patchItem(Integer idUser, Integer itemId, ItemDto itemDto) {
        userService.checkUserById(idUser);
        Item item = itemRepository.patchItem(idUser, itemId, itemMapper.toItem(itemDto));
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getItemById(Integer itemId) {
        Item item = itemRepository.getItemById(itemId);
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getItemsByIdUser(Integer idUser) {
        userService.checkUserById(idUser);
        List<Item> items = itemRepository.getItemsByIdUser(idUser);
        return itemsToItemsDto(items);
    }

    @Override
    public List<ItemDto> searchItem(String text) {
        List<Item> items = itemRepository.searchItem(text);
        return itemsToItemsDto(items);
    }

    private List<ItemDto> itemsToItemsDto(List<Item> items) {
        List<ItemDto> itemsDto = new ArrayList<>();
        for (Item item : items) {
            itemsDto.add(itemMapper.toItemDto(item));
        }
        return itemsDto;
    }
}
