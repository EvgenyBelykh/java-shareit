package ru.practicum.shareit.item.exceptions;

public class EmptyCommentException extends RuntimeException {
    public EmptyCommentException(final String message) {
        super(message);
    }
}
