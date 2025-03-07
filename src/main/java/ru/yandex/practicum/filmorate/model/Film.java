package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validator.MinDate;

import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    //    целочисленный идентификатор
    private int id;
    //    название
    @NotBlank(message = "Имя не может быть пустым")
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
    //список с id пользователей, поставивших лайк
    private Set<Integer> like;

    public void addLike(int userId) {
        like.add(userId);
    }

    public void deleteLike(int userId) {
        like.remove(userId);
    }
}
