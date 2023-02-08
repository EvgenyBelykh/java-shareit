package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.booking.exceptions.WrongStateException;
import ru.practicum.shareit.item.exceptions.EmptyCommentException;
import ru.practicum.shareit.item.exceptions.IncorrectParameterException;
import ru.practicum.shareit.item.exceptions.ValidationItemDtoException;
import ru.practicum.shareit.user.exceptions.ValidationUserRequestDtoException;

import javax.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class ErrorHandler {
    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationPatchException(ConstraintViolationException e) {
        log.info("409 Conflicting Request");
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class,
            ValidationUserRequestDtoException.class,
            ValidationItemDtoException.class,
            EmptyCommentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e) {
        log.info("400 Bad Request");
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectParameterException(IncorrectParameterException e) {
        log.info("400 Bad Request");
        return new ErrorResponse("Ошибка с полем: " + e.getParameter());
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleIncorrectStatusException(WrongStateException e) {
        log.info("400 Bad Request");
        return new ErrorResponse(String.format(e.getMessage()));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleException(Throwable e) {
        log.info("500 Internal Server Error");
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }
}
