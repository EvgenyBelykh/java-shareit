package ru.practicum.shareit.request.mappers;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.user.model.User;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class ItemRequestMapper {

    public ItemRequestDto toItemRequestDto(ItemRequest itemRequest, List<ItemDto> itemDtoLists) {
        return new ItemRequestDto(itemRequest.getId(),
                itemRequest.getDescription(),
                itemRequest.getCreated(),
                itemDtoLists);
    }

    public ItemRequest toItemRequest(ItemRequestDto itemRequestDto, LocalDateTime created, User user){
        return new ItemRequest(itemRequestDto.getDescription(),
                created,
                user);
    }

}
