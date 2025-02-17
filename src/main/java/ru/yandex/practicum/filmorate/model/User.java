package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.annotation.NoWhitespace;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    //    целочисленный идентификатор
    private int id;
    //    электронная почта
    @NotBlank(message = "Поле \"email\" не может быть пустым")
    @Email(message = "Это не похоже на email")
    private String email;
    //    логин пользователя
    @NotBlank(message = "Поле \"Логин\" не может быть пустым")
    @NoWhitespace
    private String login;
    //    имя для отображения
    private String name;
    //    дата рождения
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
