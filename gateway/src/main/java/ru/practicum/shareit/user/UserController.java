package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.Create;
import ru.practicum.shareit.Update;
import ru.practicum.shareit.user.dto.UserRequestDto;
import ru.practicum.shareit.user.exceptions.ValidationUserRequestDtoException;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {

    private final UserClient userClient;

    @PostMapping
    public ResponseEntity<Object> add(@Validated(Create.class) @RequestBody UserRequestDto userRequestDto){
        log.info("Запрос добавления пользователя с email {}", userRequestDto.getEmail());
        return  userClient.addUser(userRequestDto);
    }

    @PatchMapping("{id}")
    public ResponseEntity<Object> patch(@PathVariable("id") Long idUser,
                                        @Validated(Update.class) @RequestBody UserRequestDto userRequestDto){
        checkPatch(userRequestDto);
        log.info("Запрос обновления пользователя с id {}", idUser);
        return userClient.patchUser(idUser, userRequestDto);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Object> remove(@PathVariable("id") Long idUser){
        log.info("Запрос удаления пользователя с id {}", idUser);
        return userClient.removeUser(idUser);
    }

    @GetMapping
    public ResponseEntity<Object> getAllUsers() {
        log.info("Запрос получения всех пользователей");
        return userClient.getAllUsers();
    }

    @GetMapping("{id}")
    public ResponseEntity<Object> getByUserId(@PathVariable("id") Long idUser) {
        log.info("Запрос получения пользователя с id {}", idUser);
        return userClient.getByUserId(idUser);
    }

    private void checkPatch(UserRequestDto userRequestDto) {
        if ((userRequestDto.getName() == null || userRequestDto.getName().isEmpty() || userRequestDto.getName().isBlank()) &&
                userRequestDto.getEmail() == null) {
            throw new ValidationUserRequestDtoException("Не задано ни одно поле для обновления пользователя");
        }
    }
}
