package ru.practicum.shareit.request.exception;

public class NoItemRequestException extends RuntimeException {
    public NoItemRequestException(final long message) {
        super("Запрос с id: " + message + " не содержится в базе");
    }
}
