package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.exception.NoItemRequestException;
import ru.practicum.shareit.request.model.ItemRequest;
import ru.practicum.shareit.request.services.ItemRequestService;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.NoUserException;
import ru.practicum.shareit.user.services.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

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
public class ItemRequestServiceTest {
    private final EntityManager entityManager;
    private final ItemRequestService itemRequestService;
    private final UserService userService;

    @Test
    public void addItemRequestTestIsOk() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);
        TypedQuery<ItemRequest> query = entityManager.createQuery("SELECT ir FROM ItemRequest ir WHERE ir.id = :id", ItemRequest.class);
        ItemRequest itemRequest = query.setParameter("id", itemRequestDto.getId()).getSingleResult();

        assertThat(itemRequest.getId(), equalTo(itemRequestDto.getId()));
        assertThat(itemRequest.getDescription(), equalTo(itemRequestDto.getDescription()));
        assertThat(itemRequest.getUser().getId(), equalTo(userDto.getId()));
    }

    @Test
    public void getAllItemRequestTestIsOk() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);
        List<ItemRequestDto> itemRequestDtoList = itemRequestService.getAll(2L);

        assertThat(itemRequestDtoList.size(), equalTo(1));
        assertThat(itemRequestDtoList.get(0).getId(), equalTo(itemRequestDto.getId()));
    }

    @Test
    public void getAllItemRequestWrongUserIdTest() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);

        assertThatThrownBy(() -> {
            itemRequestService.getAll(3L);
        }).isInstanceOf(NoUserException.class).hasMessageContaining("Пользователь с id: 3 не содержится в базе");
    }

    @Test
    public void getByRequestIdTestIsOk() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);

        ItemRequestDto curItemRequestDto = itemRequestService.getByRequestId(1L, 1L);


        assertThat(curItemRequestDto.getId(), equalTo(itemRequestDto.getId()));
        assertThat(curItemRequestDto.getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    public void getByRequestIdWrongIdUserTest() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);

        assertThatThrownBy(() -> {
            itemRequestService.getByRequestId(3L, 1L);
        }).isInstanceOf(NoUserException.class).hasMessageContaining("Пользователь с id: 3 не содержится в базе");
    }

    @Test
    public void getByRequestIdWrongIdItemRequestTest() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);

        assertThatThrownBy(() -> {
            itemRequestService.getByRequestId(1L, 2L);
        }).isInstanceOf(NoItemRequestException.class).hasMessageContaining("Запрос с id: 2 не содержится в базе");
    }

    @Test
    public void getAllByUserIdWithoutPaginationTestIsOk() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);

        List<ItemRequestDto> itemRequestDtoList = itemRequestService.getAllByUserId(1L, null, null);

        assertThat(itemRequestDtoList.size(), equalTo(1));
        assertThat(itemRequestDtoList.get(0).getId(), equalTo(itemRequestDto.getId()));
        assertThat(itemRequestDtoList.get(0).getDescription(), equalTo(itemRequestDto.getDescription()));
    }

    @Test
    public void getAllByUserWrongIdUserTest() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);

        assertThatThrownBy(() -> {
            itemRequestService.getAllByUserId(3L, null, null);
        }).isInstanceOf(NoUserException.class).hasMessageContaining("Пользователь с id: 3 не содержится в базе");
    }

    @Test
    public void getAllByUserIdWithPaginationTestIsOk() {
        ItemDto itemDto = new ItemDto(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true);

        ItemRequestDto itemRequestDto = new ItemRequestDto(1, "Необходима вещь", null, List.of(itemDto));

        UserDto ownerDto = new UserDto(1, "owner@email.com", "Owner");
        UserDto userDto = new UserDto(2, "user@email.com", "name");
        userService.add(ownerDto);
        userService.add(userDto);

        itemRequestService.add(2L, itemRequestDto);

        List<ItemRequestDto> itemRequestDtoList = itemRequestService.getAllByUserId(1L, 0, 2);

        assertThat(itemRequestDtoList.size(), equalTo(1));
        assertThat(itemRequestDtoList.get(0).getId(), equalTo(itemRequestDto.getId()));
        assertThat(itemRequestDtoList.get(0).getDescription(), equalTo(itemRequestDto.getDescription()));
    }

}
