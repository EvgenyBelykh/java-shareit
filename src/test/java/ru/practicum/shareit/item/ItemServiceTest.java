package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.services.BookingService;
import ru.practicum.shareit.item.dto.AddCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.exceptions.CommentFutureException;
import ru.practicum.shareit.item.exceptions.NoBookingCommentException;
import ru.practicum.shareit.item.exceptions.NoItemException;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.services.ItemService;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.NoItemRequestException;
import ru.practicum.shareit.request.services.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.ValidationNotFoundIdUserException;
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
public class ItemServiceTest {
    private final EntityManager entityManager;
    private final ItemService itemService;
    private final UserService userService;
    private final BookingService bookingService;
    private final ItemRequestService itemRequestService;
    private UserDto userDto;

    @BeforeEach
    void addUser() {
        userDto = new UserDto(1L, "user@email.com", "name");
        userService.add(userDto);
    }

    @Test
    public void addItemTestIsOk() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        TypedQuery<Item> query = entityManager.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(item.getId(), equalTo(itemDto.getId()));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
    }

    @Test
    public void addItemWithItemRequestTestIsOk() {
        UserDto bookerUserDto = new UserDto(2, "owner@email.com", "Owner");
        userService.add(bookerUserDto);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, null);
        itemRequestService.add(2L, itemRequestDto);

        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true, 1L);
        itemService.add(1L, itemDto);

        TypedQuery<Item> query = entityManager.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(item.getItemRequest().getId(), equalTo(1L));
    }

    @Test
    public void addItemWithWrongItemRequestTest() {
        UserDto bookerUserDto = new UserDto(2, "owner@email.com", "Owner");
        userService.add(bookerUserDto);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, null);
        itemRequestService.add(2L, itemRequestDto);

        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true, 2L);


        assertThatThrownBy(() -> {
            itemService.add(1L, itemDto);
        }).isInstanceOf(NoItemRequestException.class).hasMessageContaining("Запрос с id: 2 не содержится в базе");
    }

    @Test
    public void patchUserTestIsOk() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        itemDto.setName("Вещь2");
        itemDto.setDescription("Описание");
        itemDto.setAvailable(false);

        itemService.patch(1L, 1L, itemDto);

        TypedQuery<Item> query = entityManager.createQuery("SELECT i FROM Item i WHERE i.id = :id", Item.class);
        Item item = query.setParameter("id", itemDto.getId()).getSingleResult();

        assertThat(item.getId(), equalTo(itemDto.getId()));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    public void patchItemWrongUserTest() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        itemDto.setName("Вещь2");
        itemDto.setDescription("Описание");
        itemDto.setAvailable(false);

        assertThatThrownBy(() -> {
            itemService.patch(2L, 1L, itemDto);
        }).isInstanceOf(ValidationNotFoundIdUserException.class).hasMessageContaining("У пользователя с id: 2 пока нет вещей для шеринга");
    }

    @Test
    public void patchItemWrongItemTest() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        UserDto secondUserDto = new UserDto(2L, "user2@email.com", "name2");
        userService.add(secondUserDto);

        ItemDto secondItemDto = new ItemDto(2L, "Очень необходимая вещь 2", "Очень необходимая вещь для чего-то важного 2", true);
        itemService.add(2L, secondItemDto);

        itemDto.setName("Вещь2");
        itemDto.setDescription("Описание");
        itemDto.setAvailable(false);

        assertThatThrownBy(() -> {
            itemService.patch(1L, 2L, itemDto);
        }).isInstanceOf(ValidationNotFoundIdUserException.class).hasMessageContaining("Вещь c id: 2 не принадлежит пользователю с id: 1");
    }

    @Test
    public void getByIdItemItemTestIsOk() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        ItemDto item = itemService.getById(1L, 1L);
        assertThat(item.getId(), equalTo(itemDto.getId()));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    public void getByWrongIdItemItemTestIsOk() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        assertThatThrownBy(() -> {
            itemService.getById(2L, 1L);
        }).isInstanceOf(NoItemException.class).hasMessageContaining("Вещь с id: 2 не содержится в базе");
    }

    @Test
    public void getAllItemsByIdOwnerWithoutPaginationTestIsOk() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        ItemDto secondItemDto = new ItemDto(2L, "Очень необходимая вещь 2", "Очень необходимая вещь для чего-то важного 2", true);
        itemService.add(1L, secondItemDto);

        List<ItemDto> itemDtoList = itemService.getAllByIdUser(1L, null, null);
        assertThat(2, equalTo(itemDtoList.size()));
    }

    @Test
    public void getAllItemsByWrongIdOwnerWithoutPaginationTest() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        ItemDto secondItemDto = new ItemDto(2L, "Очень необходимая вещь 2", "Очень необходимая вещь для чего-то важного 2", true);
        itemService.add(1L, secondItemDto);

        UserDto userDto = new UserDto(2L, "user2@email.com", "name2");
        userService.add(userDto);

        assertThatThrownBy(() -> {
            itemService.getAllByIdUser(2L, null, null);
        }).isInstanceOf(ValidationNotFoundIdUserException.class)
                .hasMessageContaining("У пользователя с id: 2 пока нет вещей для шеринга");
    }

    @Test
    public void searchItemWithoutPaginationTestIsOk() {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        ItemDto secondItemDto = new ItemDto(2L, "лопата", "смеяться", true);
        itemService.add(1L, secondItemDto);

        List<ItemDto> itemDtoList= itemService.search("Вещь", null, null);

        assertThat(1, equalTo(itemDtoList.size()));
        assertThat(itemDto.getName(), equalTo(itemDtoList.get(0).getName()));
    }

    @Test
    public void addCommentTestIsOk() throws InterruptedException {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        UserDto booker = new UserDto(2L, "user2@email.com", "name2");
        userService.add(booker);

        AddBookingDto addBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusSeconds(1), LocalDateTime.now().plusDays(1));
        bookingService.add(booker.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        Thread.sleep(1000L);

        AddCommentDto addCommentDto = new AddCommentDto("Ненужная вещь");
        itemService.addComment(booker.getId(), itemDto.getId(), addCommentDto);

        TypedQuery<Comment> query = entityManager.createQuery("SELECT c FROM Comment c WHERE c.author.id = :id", Comment.class);
        Comment comment = query.setParameter("id", booker.getId()).getSingleResult();

        assertThat(comment.getText(), equalTo(addCommentDto.getText()));
    }

    @Test
    public void addCommentTestNotBookingItem() throws InterruptedException {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        UserDto booker = new UserDto(2L, "user2@email.com", "name2");
        userService.add(booker);

        AddBookingDto addBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusSeconds(1), LocalDateTime.now().plusDays(1));
        bookingService.add(booker.getId(), addBookingDto);

        AddCommentDto addCommentDto = new AddCommentDto("Ненужная вещь");


        assertThatThrownBy(() -> {
            itemService.addComment(booker.getId(), itemDto.getId(), addCommentDto);;
        }).isInstanceOf(NoBookingCommentException.class)
                .hasMessageContaining("Пользователь еще не арендовал эту вещь");
    }

    @Test
    public void addCommentFutureBookingTest() throws InterruptedException {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        UserDto booker = new UserDto(2L, "user2@email.com", "name2");
        userService.add(booker);

        AddBookingDto addBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusSeconds(10), LocalDateTime.now().plusDays(1));
        bookingService.add(booker.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        AddCommentDto addCommentDto = new AddCommentDto("Ненужная вещь");

        assertThatThrownBy(() -> {
            itemService.addComment(booker.getId(), itemDto.getId(), addCommentDto);
        }).isInstanceOf(CommentFutureException.class)
                .hasMessageContaining("Нельзя делать отзыв к еще не взятой в аренду вещи");
    }

    @Test
    public void getByIdWithBookingTestIsOk() throws InterruptedException {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        UserDto booker = new UserDto(2L, "user2@email.com", "name2");
        userService.add(booker);

        AddBookingDto addBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusSeconds(1), LocalDateTime.now().plusDays(1));
        bookingService.add(booker.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        Thread.sleep(1000L);

        AddBookingDto newAddBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));
        bookingService.add(booker.getId(), newAddBookingDto);
        bookingService.patch(2, 1, true);

        ItemDto item = itemService.getById(1L, 1L);
        assertThat(item.getId(), equalTo(itemDto.getId()));
        assertThat(item.getName(), equalTo(itemDto.getName()));
        assertThat(item.getDescription(), equalTo(itemDto.getDescription()));
        assertThat(item.getAvailable(), equalTo(itemDto.getAvailable()));
    }

    @Test
    public void getByIdBookerWithoutShowBookingTestIsOk() throws InterruptedException {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        UserDto booker = new UserDto(2L, "user2@email.com", "name2");
        userService.add(booker);

        AddBookingDto addBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusSeconds(1), LocalDateTime.now().plusDays(1));
        bookingService.add(booker.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        Thread.sleep(1000L);

        AddBookingDto newAddBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));
        bookingService.add(booker.getId(), newAddBookingDto);
        bookingService.patch(2, 1, true);

        ItemDto itemDtoFromBD = itemService.getById(1L, booker.getId());

        assertThat(itemDtoFromBD.getId(), equalTo(itemDto.getId()));
    }

    @Test
    public void getAllByIdUserWithBookingWithoutPaginationTestIsOk() throws InterruptedException {
        ItemDto itemDto = new ItemDto(1L, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);
        itemService.add(1L, itemDto);

        UserDto booker = new UserDto(2L, "user2@email.com", "name2");
        userService.add(booker);

        AddBookingDto addBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusSeconds(1), LocalDateTime.now().plusDays(1));
        bookingService.add(booker.getId(), addBookingDto);
        bookingService.patch(1, 1, true);

        Thread.sleep(1000L);

        AddBookingDto newAddBookingDto = new AddBookingDto(1L, LocalDateTime.now().plusDays(2), LocalDateTime.now().plusDays(3));
        bookingService.add(booker.getId(), newAddBookingDto);
        bookingService.patch(2, 1, true);

        List<ItemDto> itemDtoFromBD = itemService.getAllByIdUser(1L, null, null);

        assertThat(itemDtoFromBD.size(), equalTo(1));
    }

}
