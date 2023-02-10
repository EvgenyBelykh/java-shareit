package ru.practicum.shareit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class CustomErrorHandlerController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<Object> handleUserNotFoundException(MethodArgumentNotValidException ex) {
        final List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        final List<CustomFieldError> customFieldErrors = new ArrayList<>();

        for (FieldError fieldError : fieldErrors) {
            final String field = fieldError.getField();

            final String message = fieldError.getDefaultMessage();

            final CustomFieldError customFieldError = CustomFieldError.builder().field(field).message(message).build();

            customFieldErrors.add(customFieldError);
        }
        log.info("400 Bad Request");
        return ResponseEntity.badRequest().body(customFieldErrors);
    }
}