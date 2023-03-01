package ru.practicum.shareit.booking.exceptions;

public class NoBookingOwnerException extends RuntimeException {
    public NoBookingOwnerException(final long message) {
        super("У владельца с id: " + message + " не забронирована ни одна вещь");
    }
}
