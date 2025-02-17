package ru.yandex.practicum.filmorate.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.yandex.practicum.filmorate.validator.NoWhiteSpaceValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = NoWhiteSpaceValidator.class)
public @interface NoWhitespace {
    String message() default "Поле не должно содержать пробелов";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}