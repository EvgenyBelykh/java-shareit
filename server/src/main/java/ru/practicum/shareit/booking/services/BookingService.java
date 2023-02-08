package ru.practicum.shareit.booking.services;

import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;

import java.util.List;

public interface BookingService {
    BookingDto add(long idUser, AddBookingDto addBookingDto);

    BookingDto patch(long bookingId, long idUser, Boolean isApproved);

    BookingDto getByIdBooking(long bookingId, long idUser);

    List<BookingDto> getAllByIdUser(long idUser, State state, Integer from, Integer size);

    List<BookingDto> getAllByIdOwner(long idUser, State state, Integer from, Integer size);
}
