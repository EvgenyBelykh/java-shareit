package ru.practicum.shareit.booking.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query(value = "SELECT b FROM Booking b WHERE b.item.id IN (SELECT DISTINCT i.id FROM Item i WHERE i.owner.id = ?1 )" +
            "ORDER BY b.start DESC")
    List<Booking> findBookingsByIdOwner(long idOwner);

    @Query(value = "SELECT b FROM Booking b WHERE b.booker.id = ?1 ORDER BY b.start DESC")
    List<Booking> findBookingsByIdUserAndSortTime(long idUser);

    Booking findFirstBookingByItemIdAndStartAfterOrderByStartAsc(long itemId, LocalDateTime start);

    Booking findTopBookingByItemIdOrderByStartAsc(long itemId);

    Optional<Booking> findFirstBookingByItemIdAndBookerIdAndStatusOrderByStartAsc(long itemId, long idUser, Status status);
}
