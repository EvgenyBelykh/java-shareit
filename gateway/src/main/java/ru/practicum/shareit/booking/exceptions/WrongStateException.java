package ru.practicum.shareit.booking.exceptions;

public class WrongStateException extends RuntimeException {
    public WrongStateException(final String message) {
        super(message);
    }
}
