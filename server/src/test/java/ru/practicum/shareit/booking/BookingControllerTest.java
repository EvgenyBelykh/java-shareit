package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import ru.practicum.shareit.booking.controllers.BookingController;
import ru.practicum.shareit.booking.dto.AddBookingDto;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.exceptions.IncorrectParameterException;
import ru.practicum.shareit.booking.exceptions.WrongStateException;
import ru.practicum.shareit.booking.services.BookingService;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
public class BookingControllerTest {
    @MockBean
    BookingService bookingService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    private BookingDto bookingDto;
    private AddBookingDto addBookingDto;
    private User user;
    private User owner;
    private Item item;
    private final LocalDateTime start = LocalDateTime.of(2024, 1, 1, 0, 0, 1).plusDays(1);
    private final LocalDateTime finish = start.plusDays(1);

    @BeforeEach
    void setup() {
        addBookingDto = new AddBookingDto(1, start, finish);
        owner = new User(1, "owner@email.com", "Owner");
        user = new User(2, "user@email.com", "name");

        item = new Item(1, "Очень необходимая вещь", "Очень необходимая вещь для чего-то важного", true, owner);
        bookingDto = new BookingDto(1, start, finish, item, owner, Status.WAITING);
    }

    @Test
    public void addNewBookingTestIsOk() throws Exception {
        when(bookingService.add(anyLong(), any())).thenReturn(bookingDto);

        mockMvc.perform(post("/bookings")
                        .content(objectMapper.writeValueAsString(addBookingDto))
                        .header("X-Sharer-User-Id", 2)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$.status", is(Status.WAITING.toString())));
    }

    @Test
    public void patchApproveBookingTestIsOk() throws Exception {
        bookingDto.setStatus(Status.APPROVED);
        when(bookingService.patch(anyLong(),anyLong(), anyBoolean())).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .content(objectMapper.writeValueAsString(addBookingDto))
                        .header("X-Sharer-User-Id", 2)
                        .param("approved", "true")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$.status", is(Status.APPROVED.toString())));
    }

    @Test
    public void patchRejectedBookingTestIsOk() throws Exception {
        bookingDto.setStatus(Status.REJECTED);
        when(bookingService.patch(anyLong(),anyLong(), anyBoolean())).thenReturn(bookingDto);

        mockMvc.perform(patch("/bookings/{bookingId}", 1)
                        .content(objectMapper.writeValueAsString(addBookingDto))
                        .header("X-Sharer-User-Id", 2)
                        .param("approved", "false")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$.status", is(Status.REJECTED.toString())));
    }

    @Test
    public void getByIdBookingTestIsOk() throws Exception {
        when(bookingService.getByIdBooking(anyLong(),anyLong())).thenReturn(bookingDto);

        mockMvc.perform(get("/bookings/{bookingId}", 1)
                        .header("X-Sharer-User-Id", 2)
                        .param("approved", "false")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$.start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$.end", is(bookingDto.getEnd().toString())))
                .andExpect(jsonPath("$.item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$.item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$.booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$.status", is(Status.WAITING.toString())));

        verify(bookingService).getByIdBooking(anyLong(),anyLong());
    }

    @Test
    public void getAllByIdBookingAllStateWithoutPaginationTestIsOk() throws Exception {
        when(bookingService.getAllByIdUser(user.getId(), State.ALL, null, null)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$[0].item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$[0].item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$[0].status", is(Status.WAITING.toString())));

        verify(bookingService).getAllByIdUser(user.getId(), State.ALL, null, null);
    }

    @Test
    public void getAllByIdBookingEmptyStateWithoutPaginationTestIsOk() throws Exception {
        when(bookingService.getAllByIdUser(user.getId(), State.ALL, null, null)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$[0].item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$[0].item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$[0].status", is(Status.WAITING.toString())));

        verify(bookingService).getAllByIdUser(user.getId(), State.ALL, null, null);
    }

    @Test
    public void getAllByIdBookingWaitingStateWithoutPaginationTestIsOk() throws Exception {
        when(bookingService.getAllByIdUser(user.getId(), State.WAITING, null, null)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .param("state", "WAITING"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$[0].item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$[0].item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$[0].status", is(Status.WAITING.toString())));

        verify(bookingService).getAllByIdUser(user.getId(), State.WAITING, null, null);
    }

    @Test
    public void getAllByIdBookingUnsupportedStateWithoutPaginationTest() throws Exception {
        when(bookingService.getAllByIdUser(anyLong(), any(), anyInt(), anyInt())).thenThrow(WrongStateException.class);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .param("state", "Unsupported"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    public void getAllByIdBookingAllStateWithPaginationTestIsOk() throws Exception {
        when(bookingService.getAllByIdUser(user.getId(), State.ALL, 0, 2)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .param("state", "ALL")
                        .param("from", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$[0].item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$[0].item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$[0].status", is(Status.WAITING.toString())));

        verify(bookingService).getAllByIdUser(user.getId(), State.ALL, 0, 2);
    }

    @Test
    public void getAllByIdBookingAllStateWithWrongPaginationTest() throws Exception {
        when(bookingService.getAllByIdUser(user.getId(), State.ALL, -1, 0)).thenThrow(IncorrectParameterException.class);

        mockMvc.perform(get("/bookings")
                        .header("X-Sharer-User-Id", 2)
                        .param("state", "Unsupported")
                        .param("from", "-1")
                        .param("size", "0"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }

    @Test
    public void getAllByIdOwnerAllStateWithoutPaginationTestIsOk() throws Exception {
        when(bookingService.getAllByIdOwner(owner.getId(), State.ALL, null, null)).thenReturn(List.of(bookingDto));

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "ALL"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingDto.getStart().toString())))
                .andExpect(jsonPath("$[0].item.id", is((int)bookingDto.getItem().getId())))
                .andExpect(jsonPath("$[0].item.name", is(bookingDto.getItem().getName())))
                .andExpect(jsonPath("$[0].booker.id", is((int)bookingDto.getBooker().getId())))
                .andExpect(jsonPath("$[0].status", is(Status.WAITING.toString())));

        verify(bookingService).getAllByIdOwner(owner.getId(), State.ALL, null, null);
    }

    @Test
    public void getAllByIdOwnerAllStateWithWrongPaginationTest() throws Exception {
        when(bookingService.getAllByIdOwner(user.getId(), State.ALL, -1, 0)).thenThrow(IncorrectParameterException.class);

        mockMvc.perform(get("/bookings/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "Unsupported")
                        .param("from", "-1")
                        .param("size", "0"))
                .andDo(MockMvcResultHandlers.print())
                .andExpect(status().is(400));
    }





}
