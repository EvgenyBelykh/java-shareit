package ru.practicum.shareit.user;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    List<User> getUsers();
    User getUserById(int idUser);
    User addUser(User user);
    User patchUser(int idUser, User user);
    void removeUser(int idUser);
    void checkUserById(Integer idUser);
}
