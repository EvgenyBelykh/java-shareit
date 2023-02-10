package ru.practicum.shareit.user;

import org.springframework.http.HttpStatus;

import java.lang.annotation.*;
import javax.validation.*;

import static java.lang.annotation.ElementType.FIELD;

@Constraint(validatedBy = UniqueEmailValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ FIELD})
public @interface UniqueEmail {
    String message() default "Пользователь с данным email уже существует";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default{};
}
