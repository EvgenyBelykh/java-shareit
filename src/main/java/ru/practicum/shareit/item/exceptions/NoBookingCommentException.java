package ru.practicum.shareit.item.exceptions;

public class NoBookingCommentException extends RuntimeException {
    public NoBookingCommentException(final String message) {
        super(message);
    }
}
