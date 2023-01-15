package ru.practicum.shareit.item.exceptions;

public class CommentFutureException extends RuntimeException {
    public CommentFutureException(final String message) {
        super(message);
    }
}
