package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import ru.practicum.shareit.user.controllers.UserController;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.exceptions.NoUserException;
import ru.practicum.shareit.user.exceptions.ValidationUserDtoException;
import ru.practicum.shareit.user.services.UserService;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
public class UserControllerTest {
    @MockBean
    UserService userService;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MockMvc mockMvc;
    private UserDto userDto;

    @BeforeEach
    void setup() {
        userDto = new UserDto(1L, "user@email.com", "name");
    }

    @Test
    public void addNewUserTestIsOk() throws Exception {
        when(userService.add(any())).thenReturn(userDto);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));

        verify(userService).add(any());
    }

    @Test
    public void addNewUserNullNameTest() throws Exception {
        userDto.setName(null);
        when(userService.add(userDto)).thenThrow(RuntimeException.class);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    public void addNewUserEmptyNameTest() throws Exception {
        userDto.setName(" ");
        when(userService.add(userDto)).thenThrow(RuntimeException.class);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    public void addNewUserWrongEmailTest() throws Exception {
        userDto.setEmail("email.ru");
        when(userService.add(userDto)).thenThrow(RuntimeException.class);

        mockMvc.perform(post("/users")
                        .content(objectMapper.writeValueAsString(userDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }


    @Test
    public void patchUserTestIsOk() throws Exception {
        UserDto updateUserDto = new UserDto(userDto.getId(), "newUser@email.com", "newName");

        when(userService.patch(anyLong(), any())).thenReturn(updateUserDto);

        mockMvc.perform(patch("/users/{id}", 1)
                        .content(objectMapper.writeValueAsString(updateUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(updateUserDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(updateUserDto.getName())))
                .andExpect(jsonPath("$.email", is(updateUserDto.getEmail())));

        verify(userService).patch(anyLong(), any());
    }

    @Test
    public void patchUserWrongEmailTest() throws Exception {
        UserDto updateUserDto = new UserDto(userDto.getId(), "email.com", "newName");

        when(userService.patch(anyLong(), any())).thenThrow(RuntimeException.class);
        ;

        mockMvc.perform(patch("/users/{id}", 1)
                        .content(objectMapper.writeValueAsString(updateUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    public void patchUserWithoutChangeTest() throws Exception {
        UserDto updateUserDto = userDto;

        when(userService.patch(anyLong(), any())).thenThrow(ValidationUserDtoException.class);
        ;

        mockMvc.perform(patch("/users/{id}", 1)
                        .content(objectMapper.writeValueAsString(updateUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    public void patchUserWithoutChangeWithNullEmailAndNameTest() throws Exception {
        UserDto updateUserDto = new UserDto(userDto.getId(), null, null);;

        when(userService.patch(anyLong(), any())).thenThrow(ValidationUserDtoException.class);
        ;

        mockMvc.perform(patch("/users/{id}", 1)
                        .content(objectMapper.writeValueAsString(updateUserDto))
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    public void getAllUsersTest() throws Exception {
        when(userService.get())
                .thenReturn(List.of(userDto));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(userDto.getName())))
                .andExpect(jsonPath("$[0].email", is(userDto.getEmail())));
    }

    @Test
    public void getByIdTest() throws Exception {
        when(userService.getById(anyLong()))
                .thenReturn(userDto);

        mockMvc.perform(get("/users/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userDto.getId()), Long.class))
                .andExpect(jsonPath("$.name", is(userDto.getName())))
                .andExpect(jsonPath("$.email", is(userDto.getEmail())));
    }

    @Test
    public void getByWrongIdTest() throws Exception {
        when(userService.getById(anyLong()))
                .thenThrow(IllegalArgumentException.class);

        mockMvc.perform(get("/users/{id}", 2))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(500));
    }

    @Test
    public void removeByIdTest() throws Exception {
        mockMvc.perform(delete("/users/{id}", anyLong()))
                .andExpect(status().isOk());

        verify(userService).remove(anyLong());
    }

    @Test
    public void removeByNotExistIdTest() throws Exception {
        doThrow(NoUserException.class).when(userService).remove(anyLong());

        mockMvc.perform(delete("/users/{id}", anyLong()))
                .andExpect(status().isNotFound());

        verify(userService).remove(anyLong());
    }
}
