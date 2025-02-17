package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.annotation.MinDate;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    //    целочисленный идентификатор
    private int id;
    //    название
    @NotBlank(message = "Поле \"Имя\" не может быть пустым")
    private String name;
    //    описание
    @Size(max = 200, message = "Длина описания не должна превышать 200 символов")
    private String description;
    //    дата релиза
    @MinDate(minDate = "28.12.1895", message = "Фильм не может быть снять раньше 28 декабря 1895 года")
    private LocalDate releaseDate;
    //    продолжительность фильма
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;

}
