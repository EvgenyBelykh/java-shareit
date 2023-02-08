package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.NoUserException;
import ru.practicum.shareit.user.mapper.UserMapper;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.services.UserService;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest(
        properties = "db.name=test",
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class UserServiceTest {
    private final EntityManager entityManager;
    private final UserService userService;
    private final UserMapper userMapper;

    @Test
    public void addUserTestIsOk() {
        UserDto userDto = new UserDto(1L, "user@email.com", "name");
        userService.add(userDto);

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User user = query.setParameter("id", userDto.getId()).getSingleResult();

        assertThat(user.getId(), equalTo(userDto.getId()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getName(), equalTo(userDto.getName()));
    }

    @Test
    public void getAllUsersTestIsOk() {
        List<UserDto> usersDtoList = List.of(
                new UserDto(1, "ivanov@email.ru", "Ivanov"),
                new UserDto(2, "petrov@email.ru", "Petrov"),
                new UserDto(3, "sidorov@email.ru", "Sidorov")
        );

        for (UserDto userDto : usersDtoList) {
            User entity = userMapper.toUser(userDto);
            entityManager.merge(entity);
        }
        entityManager.flush();

        List<UserDto> targetUsersList = userService.get();

        assertThat(targetUsersList, hasSize(usersDtoList.size()));
        for (UserDto userDto : usersDtoList) {
            assertThat(targetUsersList, hasItem(allOf(
                    hasProperty("id", equalTo(userDto.getId())),
                    hasProperty("email", equalTo(userDto.getEmail())),
                    hasProperty("name", equalTo(userDto.getName()))
            )));
        }
    }

    @Test
    public void getByIdTestIsOk() {
        List<UserDto> usersDtoList = List.of(
                new UserDto(1, "ivanov@email.ru", "Ivanov"),
                new UserDto(2, "petrov@email.ru", "Petrov"),
                new UserDto(3, "sidorov@email.ru", "Sidorov")
        );

        for (UserDto userDto : usersDtoList) {
            User entity = userMapper.toUser(userDto);
            entityManager.merge(entity);
        }
        entityManager.flush();

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        UserDto user = userMapper.toUserDto(query.setParameter("id", 2L).getSingleResult());

        assertThat(user.getId(), equalTo(usersDtoList.get(1).getId()));
        assertThat(user.getEmail(), equalTo(usersDtoList.get(1).getEmail()));
        assertThat(user.getName(), equalTo(usersDtoList.get(1).getName()));
    }

    @Test
    public void getByWrongIdTest() {
        List<UserDto> usersDtoList = List.of(
                new UserDto(1, "ivanov@email.ru", "Ivanov"),
                new UserDto(2, "petrov@email.ru", "Petrov"),
                new UserDto(3, "sidorov@email.ru", "Sidorov")
        );

        for (UserDto userDto : usersDtoList) {
            User entity = userMapper.toUser(userDto);
            entityManager.merge(entity);
        }
        entityManager.flush();

        assertThatThrownBy(() -> {
            userService.getById(4L);
        }).isInstanceOf(NoUserException.class).hasMessageContaining("Пользователь с id: 4 не содержится в базе");
    }

    @Test
    public void patchUserTestIsOk() {
        UserDto userDto = new UserDto(1L, "user@email.com", "name");
        userService.add(userDto);

        userDto.setEmail("newUser@email.com");
        userDto.setName("newName");

        userService.patch(1L, userDto);

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User user = query.setParameter("id", userDto.getId()).getSingleResult();

        assertThat(user.getId(), equalTo(userDto.getId()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getName(), equalTo(userDto.getName()));
    }

    @Test
    public void removeUserTestIsOk() {
        UserDto userDto = new UserDto(1L, "user@email.com", "name");
        userService.add(userDto);

        userService.remove(1L);

        assertThatThrownBy(() -> {
            userService.getById(1L);
        }).isInstanceOf(NoUserException.class).hasMessageContaining("Пользователь с id: 1 не содержится в базе");
    }

    @Test
    public void isExistUserTestIsOk() {
        UserDto userDto = new UserDto(1L, "user@email.com", "name");
        userService.add(userDto);

        userService.isExistUser(1L);

        TypedQuery<User> query = entityManager.createQuery("SELECT u FROM User u WHERE u.id = :id", User.class);
        User user = query.setParameter("id", userDto.getId()).getSingleResult();

        assertThat(user.getId(), equalTo(userDto.getId()));
        assertThat(user.getEmail(), equalTo(userDto.getEmail()));
        assertThat(user.getName(), equalTo(userDto.getName()));
    }
}
