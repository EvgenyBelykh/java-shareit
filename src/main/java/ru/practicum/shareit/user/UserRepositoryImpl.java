package ru.practicum.shareit.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.user.exceptions.NoUserException;
import ru.practicum.shareit.user.exceptions.ExistEmailUserDtoException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository
@Slf4j
public class UserRepositoryImpl implements UserRepository {
    private long id = 1;
    private final Map<Long, User> users = new LinkedHashMap<>();

    @Override
    public List<User> getAll() {
        List<User> curUsers = new ArrayList<>(users.values());
        log.info("Возвращен список пользователей размером: {}", curUsers.size());
        return curUsers;
    }

    @Override
    public Optional<User> getById(long idUser) {
        return Optional.ofNullable(users.get(idUser));
    }

    @Override
    public User add(User user) {
        checkExistEmail(user);

        user.setId(getId());
        users.put(user.getId(), user);
        log.info("Добавлен пользователь с id: {}", user.getId());
        return user;
    }

    @Override
    public User patch(long idUser, User user) {
        isExistUser(idUser);
        User curUser = users.get(idUser);

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            checkExistEmail(user);
            curUser.setEmail(user.getEmail());
            log.info("У пользователя с id: {} обновлен email: {}", idUser, curUser.getEmail());
        }

        if (user.getName() != null && !user.getName().isBlank()) {
            curUser.setName(user.getName());
            log.info("У пользователя с id: {} обновлено имя: {}", idUser, curUser.getName());
        }
        return curUser;
    }

    @Override
    public void remove(long idUser) {
        isExistUser(idUser);
        users.remove(idUser);
        log.info("Удален пользователь с id: {}", idUser);
    }

    @Override
    public boolean isExistUser(long idUser) {
        if (users.containsKey(idUser)) {
            return true;
        } else {
            throw new NoUserException(idUser);
        }
    }

    private void checkExistEmail(User user) {
        for (User curUser : users.values()) {
            if (curUser.getEmail().equals(user.getEmail()) && curUser.getId() != user.getId()) {
                throw new ExistEmailUserDtoException("Пользователь с email: " + user.getEmail() + " уже существует");
            }
        }
    }

    private long getId() {
        return id++;
    }
}
