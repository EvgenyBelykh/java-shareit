package ru.practicum.shareit.booking;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repositories.BookingRepository;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repositories.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repositories.UserRepository;

import java.time.LocalDateTime;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class BookingRepositoryTest {
    @Autowired
    private TestEntityManager em;

    @Autowired
    BookingRepository bookingRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void contextLoads() {
        Assertions.assertNotNull(em);
    }

    @Test
    void verifyBootstrappingByPersistingAnBooking() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        Assertions.assertEquals(0, booking.getId());
        em.persist(booking);
        Assertions.assertEquals(1, booking.getId());
    }

    @Test
    void verifyRepositoryByPersistingAnBooking() {
        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));

        Assertions.assertEquals(0, booking.getId());
        bookingRepository.save(booking);
        Assertions.assertEquals(1, booking.getId());
    }

    @Test
    void findBookingByIdOwnerTest() {
        User firstUser = new User();
        firstUser.setName("ИмяРек №1");
        firstUser.setEmail("user1@email.ru");
        userRepository.save(firstUser);

        Item firstItem = new Item();
        firstItem.setName("Вещь №1");
        firstItem.setDescription("Необходимая вещь №1");
        firstItem.setAvailable(true);
        firstItem.setOwner(firstUser);
        itemRepository.save(firstItem);

        User secondUser = new User();
        secondUser.setName("ИмяРек №2");
        secondUser.setEmail("user2@email.ru");
        userRepository.save(secondUser);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(firstItem);
        booking.setBooker(secondUser);
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);

        Assertions.assertEquals(1,
                bookingRepository.findBookingByIdOwner(firstUser.getId()).size());
        Assertions.assertEquals(booking.getId(),
                bookingRepository.findBookingByIdOwner(firstUser.getId()).get(0).getId());

        Assertions.assertEquals(1,
                bookingRepository.findBookingByIdOwner(firstUser.getId(), PageRequest.of(0, 1)).getContent().size());
        Assertions.assertEquals(booking.getId(),
                bookingRepository.findBookingByIdOwner(firstUser.getId(),
                        PageRequest.of(0, 1)).getContent().get(0).getId());
    }

    @Test
    void findBookingByIdUserTest() {
        User firstUser = new User();
        firstUser.setName("ИмяРек №1");
        firstUser.setEmail("user1@email.ru");
        userRepository.save(firstUser);

        Item firstItem = new Item();
        firstItem.setName("Вещь №1");
        firstItem.setDescription("Необходимая вещь №1");
        firstItem.setAvailable(true);
        firstItem.setOwner(firstUser);
        itemRepository.save(firstItem);

        User secondUser = new User();
        secondUser.setName("ИмяРек №2");
        secondUser.setEmail("user2@email.ru");
        userRepository.save(secondUser);

        Booking booking = new Booking();
        booking.setStart(LocalDateTime.now().plusDays(1));
        booking.setEnd(LocalDateTime.now().plusDays(2));
        booking.setItem(firstItem);
        booking.setBooker(secondUser);
        booking.setStatus(Status.APPROVED);
        bookingRepository.save(booking);

        Assertions.assertEquals(1,
                bookingRepository.findBookingByIdUserAndSortTime(secondUser.getId()).size());
        Assertions.assertEquals(booking.getId(),
                bookingRepository.findBookingByIdUserAndSortTime(secondUser.getId()).get(0).getId());

        Assertions.assertEquals(1,
                bookingRepository.findBookingByBookerId(secondUser.getId(), PageRequest.of(0, 1)).getContent().size());
        Assertions.assertEquals(booking.getId(),
                bookingRepository.findBookingByBookerId(secondUser.getId(),
                        PageRequest.of(0, 1)).getContent().get(0).getId());
    }
}
