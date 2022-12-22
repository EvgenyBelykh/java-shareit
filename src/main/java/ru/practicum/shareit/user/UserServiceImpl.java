package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.NoUserException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> get() {
        return userRepository.getAll().stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getById(long idUser) {
        User user = userRepository.getById(idUser).orElseThrow(() -> new NoUserException(idUser));
        log.info("Возвращен пользователь с id: {}", idUser);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto add(UserDto userDto) {
        User user = userRepository.add(userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto patch(long idUser, UserDto userDto) {
        User user = userRepository.patch(idUser, userMapper.toUser(userDto));
        return userMapper.toUserDto(user);
    }

    @Override
    public void remove(long idUser) {
        userRepository.remove(idUser);
    }

    @Override
    public boolean isExistUser(long idUser) {
        return userRepository.isExistUser(idUser);
    }
}
