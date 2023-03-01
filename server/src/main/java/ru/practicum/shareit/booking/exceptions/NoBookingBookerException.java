package ru.practicum.shareit.booking.exceptions;

public class NoBookingBookerException extends RuntimeException {
    public NoBookingBookerException(final long message) {
        super("Пользователь с id: " + message + " не бронировал ни одну вещь");
    }
}
