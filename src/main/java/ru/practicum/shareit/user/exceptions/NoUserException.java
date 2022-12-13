package ru.practicum.shareit.user.exceptions;

public class NoUserException extends RuntimeException {
    public NoUserException(final long message) {
        super("Пользователь с id: " + message + " не содержится в базе");
    }
}
