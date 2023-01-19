package ru.practicum.shareit.item.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.model.LastBooking;
import ru.practicum.shareit.item.model.NextBooking;
import ru.practicum.shareit.user.model.User;

import java.util.List;

@Component
public class ItemMapper {

    public ItemDto toItemDtoWithoutBooking(Item item) {
        return new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable());
    }

    public ItemDto toItemDtoWithoutBookingWithComments(Item item, List<CommentDto> comments) {
        return new ItemDto(item.getId(),
                item.getName(),
                item.getDescription(),
                item.getAvailable(),
                comments);
    }

    public ItemDto toItemDtoWithBookingWithComments(Item item, LastBooking lastBooking, NextBooking nextBooking,
                                                    List<CommentDto> comments) {
        return new ItemDto(item.getId(),
                item.getName(), item.getDescription(),
                item.getAvailable(),
                lastBooking,
                nextBooking,
                comments);
    }

    public Item toItem(ItemDto itemDto, User owner) {
        return new Item(itemDto.getId(),
                itemDto.getName(),
                itemDto.getDescription(),
                itemDto.getAvailable(),
                owner);
    }
}
