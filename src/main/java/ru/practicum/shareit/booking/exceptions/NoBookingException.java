package ru.practicum.shareit.booking.exceptions;

public class NoBookingException extends RuntimeException {
    public NoBookingException(final long message) {
        super("Бронирование с id: " + message + " не содержится в базе");
    }
}
