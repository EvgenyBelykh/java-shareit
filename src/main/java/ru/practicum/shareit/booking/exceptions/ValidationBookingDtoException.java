package ru.practicum.shareit.booking.exceptions;

public class ValidationBookingDtoException extends RuntimeException {
    public ValidationBookingDtoException(final String message) {
        super(message);
    }
}
