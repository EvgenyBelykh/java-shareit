package ru.practicum.shareit.user.exceptions;

public class ValidationUserDtoException extends RuntimeException {
    public ValidationUserDtoException(final String message) {
        super(message);
    }
}
