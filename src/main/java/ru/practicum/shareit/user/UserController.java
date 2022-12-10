package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.ValidationUserDtoException;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
public class UserController {
    UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getUsers() {
        log.info("Запрос получения всех пользователей");
        return userService.getUsers();
    }

    @GetMapping("{id}")
    public UserDto getUserById(@PathVariable("id") Integer idUser) {
        log.info("Запрос получения пользователя с id {}", idUser);
        return userService.getUserById(idUser);
    }

    @PostMapping
    public UserDto addUser(@Valid @RequestBody UserDto userDto) {
        validNameUserDto(userDto);
        validEmailUserDto(userDto);
        log.info("Запрос добавления пользователя с email {}", userDto.getEmail());
        return userService.addUser(userDto);
    }

    @PatchMapping("{id}")
    public UserDto patchUser(@PathVariable("id") Integer idUser,
                             @Valid @RequestBody UserDto userDto) {
        checkPatchUserDto(userDto);
        log.info("Запрос обновления пользователя с id {}", idUser);
        return userService.patchUser(idUser, userDto);
    }

    @DeleteMapping("{id}")
    public void removeUser(@PathVariable("id") Integer idUser) {
        log.info("Запрос удаления пользователя с id {}", idUser);
        userService.removeUser(idUser);
    }

    private void validNameUserDto(UserDto userDto) {
        if (userDto.getName() == null) {
            throw new ValidationUserDtoException("Пользователю не задано имя");
        }
        if (userDto.getName().isEmpty() || userDto.getName().isBlank()) {
            throw new ValidationUserDtoException("Имя пользователя не может быть пустым или состоять только из пробелов");
        }
    }

    private void validEmailUserDto(UserDto userDto) {
        if (userDto.getEmail() == null) {
            throw new ValidationUserDtoException("Пользователю не задан email");
        }
    }

    private void checkPatchUserDto(UserDto userDto) {
        if ((userDto.getName() == null || userDto.getName().isEmpty() || userDto.getName().isBlank()) &&
                userDto.getEmail() == null) {
            throw new ValidationUserDtoException("Не задано ни одно поле для обновления пользователя");
        }
    }
}
