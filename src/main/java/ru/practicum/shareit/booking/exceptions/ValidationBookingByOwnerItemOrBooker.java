package ru.practicum.shareit.booking.exceptions;

public class ValidationBookingByOwnerItemOrBooker extends RuntimeException {
    public ValidationBookingByOwnerItemOrBooker(final String message) {
        super(message);
    }
}
