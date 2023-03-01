package ru.practicum.shareit.request.services;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestDto add(Long idUser, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> getAll(Long idUser);

    ItemRequestDto getByRequestId(Long idUser, Long requestId);

    List<ItemRequestDto> getAllByUserId(Long idUser, Integer from, Integer size);
}
