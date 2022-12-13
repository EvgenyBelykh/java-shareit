package ru.practicum.shareit;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class CustomErrorHandlerController {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public final ResponseEntity<Object> handleUserNotFoundException(MethodArgumentNotValidException ex, WebRequest request) {
        final List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
        final List<CustomFieldError> customFieldErrors = new ArrayList<>();

        for (FieldError fieldError: fieldErrors) {
            final String field = fieldError.getField();

            final String message = fieldError.getDefaultMessage();

            final CustomFieldError customFieldError = CustomFieldError.builder().field(field).message(message).build();

            customFieldErrors.add(customFieldError);
        }

        return ResponseEntity.badRequest().body(customFieldErrors);
    }
}
