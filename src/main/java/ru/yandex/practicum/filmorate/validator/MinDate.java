package ru.yandex.practicum.filmorate.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD}) // Аннотация может применяться только к полям
@Retention(RetentionPolicy.RUNTIME) // Аннотация доступна в runtime
@Constraint(validatedBy = MinDateValidator.class) // Указываем класс-валидатор
public @interface MinDate {
    String message() default "Дата должна быть не раньше {minDate}"; // Сообщение об ошибке по умолчанию

    Class<?>[] groups() default {}; // Группы валидации

    Class<? extends Payload>[] payload() default {}; // Полезная нагрузка (например, для кастомных данных)

    String minDate(); // Параметр аннотации: минимальная допустимая дата
}