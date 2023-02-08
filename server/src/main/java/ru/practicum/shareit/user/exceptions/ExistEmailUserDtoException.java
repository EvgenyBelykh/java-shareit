package ru.practicum.shareit.user.exceptions;

public class ExistEmailUserDtoException extends RuntimeException {
    public ExistEmailUserDtoException(final String message) {
        super(message);
    }
}
