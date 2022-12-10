package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Qualifier(value = "UserRepositoryImpl")
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper userMapper = new UserMapper();

    public List<UserDto> getUsers() {
        List<User> userList = repository.getUsers();

        List<UserDto> userDtoList = new ArrayList<>();
        for (User user : userList) {
            userDtoList.add(userMapper.toUserDto(user));
        }
        return userDtoList;
    }

    public UserDto getUserById(Integer idUser) {
        return userMapper.toUserDto(repository.getUserById(idUser));
    }

    public UserDto addUser(UserDto userDto) {
        User user = repository.addUser(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    public UserDto patchUser(Integer idUser, UserDto userDto) {
        User user = repository.patchUser(idUser, userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    public void removeUser(Integer idUser) {
        repository.removeUser(idUser);
    }

    @Override
    public void checkUserById(Integer idUser) {
        repository.checkUserById(idUser);
    }
}
