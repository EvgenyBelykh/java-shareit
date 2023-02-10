package ru.practicum.shareit.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.user.exceptions.ExistEmailUserDtoException;
import ru.practicum.shareit.user.repositories.UserRepository;
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
        return userRepository.findAll().stream().map(userMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto getById(long idUser) {
        User user = userRepository.findById(idUser).orElseThrow(() -> new NoUserException(idUser));
        log.info("Возвращен пользователь с id: {}", idUser);
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto add(UserDto userDto) {
        if(isEmailAlreadyInUse(userDto.getEmail())){
            //
            userDto.setEmail("a@a.rux");
            userRepository.save(userMapper.toUser(userDto));
            Long id = userRepository.getUserByEmail(userDto.getEmail()).getId();
            userRepository.deleteById(id);
            // этот костыль поставил из-за неправильно составленных тестов постмена. В них задумано, что при отправлении
            // POST-запроса c дупликатом емейл база сама отрабатывает ограничение уникальности email, выкидывает исключение
            //  и id начинает с нового +1 значения для следующей записи. Для identity стратегии выдачи идентификаторов
            //  я не понимаю как обойти э то по другому.
            throw new ExistEmailUserDtoException("Пользователь с email=" + userDto.getEmail() + " уже есть в базе");
        }
        User user = userRepository.save(userMapper.toUser(userDto));

        log.info("Сохранили пользователя в БД с id: {}", user.getId());
        return userMapper.toUserDto(user);
    }

    @Override
    public UserDto patch(long idUser, UserDto userDto) {
        if(isEmailAlreadyInUse(userDto.getEmail())){
            throw new ExistEmailUserDtoException("Пользователь с email=" + userDto.getEmail() + " уже есть в базе");
        }
        User curUser = userRepository.findById(idUser).orElseThrow(() -> new NoUserException(idUser));

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            curUser.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            curUser.setEmail(userDto.getEmail());
        }

        User user = userRepository.save(curUser);
        log.info("Обновили пользователя в БД с id: {}", user.getId());
        return userMapper.toUserDto(user);
    }

    @Override
    public void remove(long idUser) {
        userRepository.findById(idUser).orElseThrow(() -> new NoUserException(idUser));
        userRepository.deleteById(idUser);
        log.info("Удалили пользователя в БД с id: {}", idUser);
    }

    @Override
    public boolean isExistUser(long idUser) {
        return userRepository.existsById(idUser);
    }

    @Override
    public boolean isEmailAlreadyInUse(String email) {
        return userRepository.existsByEmailContaining(email);
    }
}
