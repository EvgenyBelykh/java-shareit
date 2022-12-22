package ru.practicum.shareit.item.exceptions;

public class NoItemException extends RuntimeException {
    public NoItemException(final long message) {
        super("Пользователь с id: " + message + " не содержится в базе");
    }
}