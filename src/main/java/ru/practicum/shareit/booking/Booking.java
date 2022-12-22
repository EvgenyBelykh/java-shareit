package ru.practicum.shareit.booking;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Booking {

    int id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDateTime start;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDateTime end;
    Item item;
    User booker;
    Status status;

}
