package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.exceptions.*;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.services.BookingService;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.NoItemException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.services.ItemService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.NoUserException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingServiceTest {
    private final EntityManager entityManager;

    private final BookingService bookingService;
    private final ItemService itemService;
    private final UserService userService;
    private final LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0, 1);
    private final LocalDateTime finish = start.plusDays(1);

    @Test
    public void addBookingTestIsOk() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);


        User owner = new User(1, "owner@email.com", "Owner");
        User user = new User(2, "user@email.com", "name");

        Item item = new Item(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true, owner);

        BookingDto bookingDto = new BookingDto(1, start, finish, item, owner, Status.WAITING);

        bookingService.add(userDto.getId(), addBookingDto);

        TypedQuery<Booking> query = entityManager.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking = query.setParameter("id", bookingDto.getId()).getSingleResult();

        assertThat(booking.getId(), equalTo(bookingDto.getId()));
        assertThat(booking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(booking.getItem(), equalTo(item));
        assertThat(booking.getBooker(), equalTo(user));
        assertThat(booking.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void addBookingWrongIdItemTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        addBookingDto.setItemId(2);

        assertThatThrownBy(() -> {
            bookingService.add(userDto.getId(), addBookingDto);
        }).isInstanceOf(NoItemException.class).hasMessageContaining("Вещь с id: 2 не содержится в базе");
    }

    @Test
    public void addBookingIdBookerLikeIdOwnerTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        assertThatThrownBy(() -> {
            bookingService.add(ownerDto.getId(), addBookingDto);
        }).isInstanceOf(ValidationBelongsItemToUser.class).hasMessageContaining("Пользователь не может взять вещь сам у себя в аренду");
    }

    @Test
    public void addBookingTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", false);
        itemService.add(ownerDto.getId(), itemDto);

        assertThatThrownBy(() -> {
            bookingService.add(userDto.getId(), addBookingDto);
        }).isInstanceOf(ValidationBookingDtoException.class)
                .hasMessageContaining("Вещь с id: 1 сейчас не доступна для бронирования");
    }

    @Test
    public void patchFalseBookingTestIsOk() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);


        User owner = new User(1, "owner@email.com", "Owner");
        User user = new User(2, "user@email.com", "name");

        Item item = new Item(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true, owner);

        BookingDto bookingDto = new BookingDto(1, start, finish, item, owner, Status.WAITING);

        bookingService.add(userDto.getId(), addBookingDto);

        bookingService.patch(1, 1, false);


        TypedQuery<Booking> query = entityManager.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking = query.setParameter("id", bookingDto.getId()).getSingleResult();

        assertThat(booking.getId(), equalTo(bookingDto.getId()));
        assertThat(booking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(booking.getItem(), equalTo(item));
        assertThat(booking.getBooker(), equalTo(user));
        assertThat(booking.getStatus(), equalTo(Status.REJECTED));
    }

    @Test
    public void patchTrueBookingTestIsOk() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);


        User owner = new User(1, "owner@email.com", "Owner");
        User user = new User(2, "user@email.com", "name");

        Item item = new Item(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true, owner);

        BookingDto bookingDto = new BookingDto(1, start, finish, item, owner, Status.WAITING);

        bookingService.add(userDto.getId(), addBookingDto);

        bookingService.patch(1, 1, true);


        TypedQuery<Booking> query = entityManager.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking = query.setParameter("id", bookingDto.getId()).getSingleResult();

        assertThat(booking.getId(), equalTo(bookingDto.getId()));
        assertThat(booking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(booking.getItem(), equalTo(item));
        assertThat(booking.getBooker(), equalTo(user));
        assertThat(booking.getStatus(), equalTo(Status.APPROVED));
    }

    @Test
    public void patchTrueBookingTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        bookingService.patch(1, 1, true);

        assertThatThrownBy(() -> {
            bookingService.patch(1, 1, true);
        }).isInstanceOf(ValidationStatusException.class)
                .hasMessageContaining("Владелец вещи с id= 1 уже подтвердил брованирование с id= 1");
    }

    @Test
    public void patchFalseBookingTestId() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        bookingService.patch(1, 1, false);

        assertThatThrownBy(() -> {
            bookingService.patch(1, 1, false);
        }).isInstanceOf(ValidationStatusException.class)
                .hasMessageContaining("Владелец вещи с id= 1 уже отклонил брованирование с id= 1");
    }

    @Test
    public void patchBookingWithNotBelongItemByUserTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        assertThatThrownBy(() -> {
            bookingService.patch(1, 2, false);
        }).isInstanceOf(ValidationBookingByOwnerItemOrBooker.class)
                .hasMessageContaining("Вещь с id= 1 не принадлежит пользователю с id= 1");
    }

    @Test
    public void patchWrongBookingTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        assertThatThrownBy(() -> {
            bookingService.patch(2, 1, false);
        }).isInstanceOf(NoBookingException.class)
                .hasMessageContaining("Бронирование с id: 2 не содержится в базе");
    }

    @Test
    public void getByIdTestIsOk() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);


        User owner = new User(1, "owner@email.com", "Owner");
        User user = new User(2, "user@email.com", "name");

        Item item = new Item(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true, owner);

        bookingService.add(userDto.getId(), addBookingDto);

        BookingDto bookingDto = bookingService.getByIdBooking(1, 1);


        TypedQuery<Booking> query = entityManager.createQuery("SELECT b FROM Booking b WHERE b.id = :id", Booking.class);
        Booking booking = query.setParameter("id", bookingDto.getId()).getSingleResult();

        assertThat(booking.getId(), equalTo(bookingDto.getId()));
        assertThat(booking.getStart(), equalTo(bookingDto.getStart()));
        assertThat(booking.getEnd(), equalTo(bookingDto.getEnd()));
        assertThat(booking.getItem(), equalTo(item));
        assertThat(booking.getBooker(), equalTo(user));
        assertThat(booking.getStatus(), equalTo(bookingDto.getStatus()));
    }

    @Test
    public void getByIdWrongIdUserTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        assertThatThrownBy(() -> {
            bookingService.getByIdBooking(1, 3);
        }).isInstanceOf(ValidationBookingByOwnerItemOrBooker.class)
                .hasMessageContaining("Пользователь с id= 3 не является владельцем или арендатором вещи с id= 1");
    }

    @Test
    public void getByIdWrongIdBookingTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        assertThatThrownBy(() -> {
            bookingService.getByIdBooking(2, 1);
        }).isInstanceOf(NoBookingException.class)
                .hasMessageContaining("Бронирование с id: 2 не содержится в базе");
    }

    @Test
    public void getAllByIdUserWithoutPaginationIsOk() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        List<BookingDto> bookingDtoList = bookingService.getAllByIdUser(2, State.ALL, null, null);

        assertThat(bookingDtoList.size(), equalTo(1));
    }

    @Test
    public void getAllByIdUserWithoutPaginationWrongIdUser() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);


        User owner = new User(1, "owner@email.com", "Owner");
        User user = new User(2, "user@email.com", "name");

        Item item = new Item(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true, owner);

        BookingDto bookingDto = new BookingDto(1, start, finish, item, owner, Status.WAITING);

        bookingService.add(userDto.getId(), addBookingDto);

        assertThatThrownBy(() -> {
            bookingService.getAllByIdUser(3, State.ALL, null, null);
            ;
        }).isInstanceOf(NoUserException.class)
                .hasMessageContaining("Пользователь с id: 3 не содержится в базе");
    }

    @Test
    public void getAllByIdUserWithoutPaginationNotBookingUser() {
        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        assertThatThrownBy(() -> {
            bookingService.getAllByIdUser(2, State.ALL, null, null);
            ;
        }).isInstanceOf(NoBookingBookerException.class)
                .hasMessageContaining("Пользователь с id: 2 не бронировал ни одну вещь");
    }

    @Test
    public void getAllByIdUserWithPaginationIsOk() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        List<BookingDto> bookingDtoList = bookingService.getAllByIdUser(2, State.ALL, 0, 2);

        assertThat(bookingDtoList.size(), equalTo(1));
    }

    @Test
    public void getAllByIdUserWithPagination() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);

        assertThatThrownBy(() -> {
            bookingService.getAllByIdUser(2, State.ALL, 2, 2);
        }).isInstanceOf(NoBookingBookerException.class)
                .hasMessageContaining("Пользователь с id: 2 не бронировал ни одну вещь");
    }

    @Test
    public void getAllByIdOwnerWithoutPaginationIsOk() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        List<BookingDto> bookingDtoList = bookingService.getAllByIdOwner(1, State.ALL, null, null);

        assertThat(bookingDtoList.size(), equalTo(1));
    }

    @Test
    public void getAllByIdOwnerWithoutPaginationNotBookingTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        assertThatThrownBy(() -> {
            bookingService.getAllByIdOwner(2, State.ALL, null, null);
        }).isInstanceOf(NoBookingOwnerException.class)
                .hasMessageContaining("У владельца с id: 2 не забронирована ни одна вещь");
    }

    @Test
    public void getAllByIdOwnerWithoutPaginationWrongIdUserTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        assertThatThrownBy(() -> {
            bookingService.getAllByIdOwner(3, State.ALL, null, null);
        }).isInstanceOf(NoUserException.class)
                .hasMessageContaining("Пользователь с id: 3 не содержится в базе");
    }

    @Test
    public void getAllByIdOwnerWithPaginationIsOk() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        List<BookingDto> bookingDtoList = bookingService.getAllByIdOwner(1, State.ALL, 0, 2);

        assertThat(bookingDtoList.size(), equalTo(1));
    }

    @Test
    public void getAllByIdOwnerWithPaginationNotBookingTest() {
        AddBookingDto addBookingDto = new AddBookingDto(1, start, finish);

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(ownerDto.getId(), itemDto);

        bookingService.add(userDto.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        assertThatThrownBy(() -> {
            bookingService.getAllByIdOwner(2, State.ALL, 0, 2);
        }).isInstanceOf(NoBookingOwnerException.class)
                .hasMessageContaining("У владельца с id: 2 не забронирована ни одна вещь");
    }
}
