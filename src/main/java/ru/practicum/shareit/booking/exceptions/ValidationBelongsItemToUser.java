package ru.practicum.shareit.booking.exceptions;

public class ValidationBelongsItemToUser extends RuntimeException {
    public ValidationBelongsItemToUser(final String message) {
        super(message);
    }
}
