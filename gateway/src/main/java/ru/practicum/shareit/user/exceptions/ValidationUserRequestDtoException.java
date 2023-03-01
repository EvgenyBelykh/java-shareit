package ru.practicum.shareit.user.exceptions;

public class ValidationUserRequestDtoException extends RuntimeException {
    public ValidationUserRequestDtoException(final String message) {
        super(message);
    }
}
