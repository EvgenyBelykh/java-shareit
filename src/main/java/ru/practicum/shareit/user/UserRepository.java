package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    List<User> getAll();

    Optional<User> getById(long idUser);

    User add(User user);

    User patch(long idUser, User user);

    void remove(long idUser);

    boolean isExistUser(long idUser);

}
