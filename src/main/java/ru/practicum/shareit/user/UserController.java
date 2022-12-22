package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.ValidationUserDtoException;

import java.util.List;

@RestController
@RequestMapping(path = "/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public List<UserDto> getAll() {
        log.info("Запрос получения всех пользователей");
        return userService.get();
    }

    @GetMapping("{id}")
    public UserDto getById(@PathVariable("id") Long idUser) {
        log.info("Запрос получения пользователя с id {}", idUser);
        return userService.getById(idUser);
    }

    @PostMapping
    public UserDto add(@Validated(Create.class) @RequestBody UserDto userDto) {
        log.info("Запрос добавления пользователя с email {}", userDto.getEmail());
        return userService.add(userDto);
    }

    @PatchMapping("{id}")
    public UserDto patch(@PathVariable("id") Long idUser,
                         @Validated(Update.class) @RequestBody UserDto userDto) {
        checkPatch(userDto);
        log.info("Запрос обновления пользователя с id {}", idUser);
        return userService.patch(idUser, userDto);
    }

    @DeleteMapping("{id}")
    public void remove(@PathVariable("id") Long idUser) {
        log.info("Запрос удаления пользователя с id {}", idUser);
        userService.remove(idUser);
    }

    private void checkPatch(UserDto userDto) {
        if ((userDto.getName() == null || userDto.getName().isEmpty() || userDto.getName().isBlank()) &&
                userDto.getEmail() == null) {
            throw new ValidationUserDtoException("Не задано ни одно поле для обновления пользователя");
        }
    }
}
