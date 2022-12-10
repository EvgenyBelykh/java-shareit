package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.practicum.shareit.user.exceptions.ValidationNotFoundIdUserException;
import ru.practicum.shareit.user.exceptions.ValidationPatchUserDtoException;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private int id = 1;
    private final Map<Integer, User> users = new HashMap<>();

    @Override
    public List<User> getUsers() {
        List<User> curUsers = new ArrayList<>(users.values());
        log.info("Возвращен список пользователей размером: {}", curUsers.size());
        return curUsers;
    }

    @Override
    public User getUserById(int idUser) {
        checkExistIdUser(idUser);
        User curUser = users.get(idUser);
        log.info("Возвращен пользователь с id: {}", idUser);
        return curUser;
    }

    @Override
    public User addUser(User user) {
        checkExistEmail(user);

        user.setId(getId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public User patchUser(int idUser, User user) {
        checkExistIdUser(idUser);

        User curUser = users.get(idUser);

        if (user.getEmail() != null && user.getName() != null) {
            checkExistEmail(user);

            curUser.setName(user.getName());
            curUser.setEmail(user.getEmail());
            users.put(idUser, curUser);
            log.info("У пользователя с id: {} обновлено имя: {} и email: {}", idUser, curUser.getName(), curUser.getEmail());
            return curUser;

        } else if (user.getEmail() != null && user.getName() == null) {
            checkExistEmail(user);
            curUser.setEmail(user.getEmail());
            users.put(idUser, curUser);
            log.info("У пользователя с id: {} обновлен email: {}", idUser, curUser.getEmail());
            return curUser;

        } else {

            curUser.setName(user.getName());
            users.put(idUser, curUser);
            log.info("У пользователя с id: {} обновлено имя: {}", idUser, curUser.getName());
            return curUser;
        }
    }

    @Override
    public void removeUser(int idUser) {
        checkExistIdUser(idUser);
        users.remove(idUser);
        log.info("Удален пользователь с id: {}", idUser);
    }

    @Override
    public void checkUserById(Integer idUser) {
        if (!users.containsKey(idUser)) {
            throw new ValidationNotFoundIdUserException("Пользователь с id: " + idUser + " не содержится в базе");
        }
    }

    private int getId() {
        return id++;
    }

    private void checkExistEmail(User user) {
        for (User value : users.values()) {
            if (value.getEmail().equals(user.getEmail())) {
                throw new ValidationPatchUserDtoException("Пользователь с email: " + user.getEmail() + " уже существует");
            }
        }
    }

    private void checkExistIdUser(int idUser) {
        if (!users.containsKey(idUser)) {
            throw new ValidationPatchUserDtoException("Пользователь с id: " + idUser + " не содержится в базе");
        }
    }
}
