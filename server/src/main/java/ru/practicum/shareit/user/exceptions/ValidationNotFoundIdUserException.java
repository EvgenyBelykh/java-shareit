package ru.practicum.shareit.user.exceptions;

public class ValidationNotFoundIdUserException extends RuntimeException {
    public ValidationNotFoundIdUserException(final String message) {
        super(message);
    }
}
