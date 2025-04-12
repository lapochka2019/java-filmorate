package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.validator.MinDate;

import java.time.LocalDate;
import java.util.HashSet;
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
    //рейтинг фильма (оценка)
    @Positive(message = "Оценка фильма должна быть положительным числом")
    private int rate;
    //возрастной рейтинг - ссылается на таблицу
    private int mpa;
    //список с id пользователей, поставивших лайк
    private Set<Integer> likes = new HashSet<>();
    //список с жанрами фильма
    private Set<Integer> genres = new HashSet<>();

    public void addLike(int userId) {
        likes.add(userId);
        rate++;
    }

    public void removeLike(int userId) {
        likes.remove(userId);
        rate--;
    }

    public void addGenre(int genreId) {
        genres.add(genreId);
    }

    public void removeGenre(int genreId) {
        genres.remove(genreId);
    }
}
