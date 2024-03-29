package ru.practicum.shareit.user.services;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> get();

    UserDto getById(long idUser);

    UserDto add(UserDto userDto);

    UserDto patch(long idUser, UserDto userDto);

    void remove(long idUser);

    boolean isExistUser(long idUser);

    boolean isEmailAlreadyInUse(String email);
}
