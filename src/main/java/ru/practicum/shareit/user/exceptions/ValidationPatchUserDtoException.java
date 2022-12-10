package ru.practicum.shareit.user.exceptions;

public class ValidationPatchUserDtoException extends RuntimeException {
    public ValidationPatchUserDtoException(final String message) {
        super(message);
    }
}
