package ru.practicum.shareit.item.services;

import ru.practicum.shareit.item.dto.AddCommentDto;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    ItemDto add(long idUser, ItemDto itemDto);

    ItemDto patch(long idUser, long itemId, ItemDto itemDto);

    ItemDto getById(long itemId, long idUser);

    List<ItemDto> getAllByIdUser(long idUser, Integer from, Integer size);

    List<ItemDto> search(String text, Integer from, Integer size);

    CommentDto addComment(long idUser, long itemId, AddCommentDto addCommentDto);
}
