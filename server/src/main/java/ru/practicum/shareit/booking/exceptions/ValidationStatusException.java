package ru.practicum.shareit.booking.exceptions;

public class ValidationStatusException extends RuntimeException {
    public ValidationStatusException(final String message) {
        super(message);
    }
}
