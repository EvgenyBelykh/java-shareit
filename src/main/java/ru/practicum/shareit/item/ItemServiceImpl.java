package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.NoItemException;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserService;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final UserService userService;
    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;
    private final UserMapper userMapper;

    @Override
    public ItemDto add(long idUser, ItemDto itemDto) {
        User user = userMapper.toUser(userService.getById(idUser));
        Item item = itemRepository.add(idUser, itemMapper.toItem(itemDto,  user));
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto patch(long idUser, long itemId, ItemDto itemDto) {
        User user = userMapper.toUser(userService.getById(idUser));
        Item item = itemRepository.patch(idUser, itemId, itemMapper.toItem(itemDto, user));
        return itemMapper.toItemDto(item);
    }

    @Override
    public ItemDto getById(long itemId) {
        Item item = itemRepository.getById(itemId).orElseThrow(() -> new NoItemException(itemId));
        log.info("Возвращена вещь с id: {}", itemId);
        return itemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> getAllByIdUser(long idUser) {
        userMapper.toUser(userService.getById(idUser));
        return itemsToItemsDto(itemRepository.getAllByIdUser(idUser));
    }

    @Override
    public List<ItemDto> search(String text) {
        return itemsToItemsDto(itemRepository.search(text));
    }

    private List<ItemDto> itemsToItemsDto(List<Item> items) {
        return items.stream().map(itemMapper::toItemDto).collect(Collectors.toList());
    }
}
