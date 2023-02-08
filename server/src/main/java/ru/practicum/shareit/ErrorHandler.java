package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exceptions.*;
import ru.practicum.shareit.item.exceptions.*;
import ru.practicum.shareit.request.exception.NoItemRequestException;
import ru.practicum.shareit.user.exceptions.NoUserException;
import ru.practicum.shareit.user.exceptions.ValidationNotFoundIdUserException;
import ru.practicum.shareit.user.exceptions.ExistEmailUserDtoException;
import ru.practicum.shareit.user.exceptions.ValidationUserDtoException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationPatchException(ExistEmailUserDtoException e) {
        log.info("409 Conflicting Request");
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }

    @ExceptionHandler({ValidationNotFoundIdUserException.class,
            ValidationNotFoundIdItemException.class,
            NoUserException.class,
            NoItemException.class,
            NoBookingException.class,
            ValidationBookingByOwnerItemOrBooker.class,
            NoBookingBookerException.class,
            NoBookingOwnerException.class,
            ValidationBelongsItemToUser.class,
            NoItemRequestException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleValidationPatchException(Exception e) {
        log.info("404 Not Found");
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class,
            ValidationItemDtoException.class,
            ValidationBookingDtoException.class,
            ValidationStatusException.class,
            CommentFutureException.class,
            NoBookingCommentException.class,
            ValidationUserDtoException.class,
            EmptyCommentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e) {
        log.info("400 Bad Request");
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectStatusException(WrongStateException e) {
        log.info("400 Bad Request");
        return new ErrorResponse(String.format(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectParameterException(IncorrectParameterException e) {
        log.info("400 Bad Request");
        return new ErrorResponse("Ошибка с полем: " + e.getParameter());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Throwable e) {
        log.info("500 Internal Server Error");
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }
}
