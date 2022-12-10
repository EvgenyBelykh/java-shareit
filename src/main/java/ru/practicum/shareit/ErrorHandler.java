package ru.practicum.shareit;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.practicum.shareit.item.exceptions.ValidationItemDtoException;
import ru.practicum.shareit.item.exceptions.ValidationNotFoundIdItemException;
import ru.practicum.shareit.user.exceptions.ValidationNotFoundIdUserException;
import ru.practicum.shareit.user.exceptions.ValidationUserDtoException;
import ru.practicum.shareit.user.exceptions.ValidationPatchUserDtoException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleValidationPatchException(ValidationPatchUserDtoException e) {
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }

    @ExceptionHandler({ValidationNotFoundIdUserException.class,
            ValidationNotFoundIdItemException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleValidationPatchException(Exception e) {
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }

    @ExceptionHandler({ValidationUserDtoException.class,
            MethodArgumentNotValidException.class,
            ValidationItemDtoException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidationException(Exception e) {
        return new ErrorResponse("Неверный запрос: " + e.getMessage());
    }

//    @ExceptionHandler({DataIntegrityViolationException.class
//            , ValidationUserByIdException.class
//            , ValidationFilmByIdException.class
//            , MethodArgumentNotValidException.class})
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public ErrorResponse handleDataIntegrityViolationException(Exception e) {
//        return new ErrorResponse("Неверный запрос: " + e.getMessage());
//    }
}
