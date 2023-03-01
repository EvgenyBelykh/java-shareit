package ru.practicum.shareit.user;

import org.springframework.beans.factory.annotation.Autowired;
import ru.practicum.shareit.user.services.UserService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class UniqueEmailValidator implements ConstraintValidator<UniqueEmail, String> {
    @Autowired
    private UserService userService;

    @Override
    public void initialize(UniqueEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value != null && !userService.isEmailAlreadyInUse(value);
    }
}
