package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getUsers();

    UserDto getUserById(Integer idUser);

    UserDto addUser(UserDto userDto);

    UserDto patchUser(Integer idUser, UserDto userDto);

    void removeUser(Integer idUser);

    void checkUserById(Integer idUser);
}
