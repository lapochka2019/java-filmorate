package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Min;
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
    @MinDate(minDate = "28.12.1895", message = "Фильм не может быть снят раньше 28 декабря 1895 года")
    private LocalDate releaseDate;
    //    продолжительность фильма
    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
    //рейтинг фильма (оценка)
    @Min(value = 0, message = "Оценка фильма не может быть отрицательной")
    private int rate;
    //возрастной рейтинг - ссылается на таблицу
    private MpaRating mpa;
    //список с id пользователей, поставивших лайк
    private Set<Integer> likes = new HashSet<>();
    //список с жанрами фильма
    private Set<Genre> genres = new HashSet<>();

    public void addLike(int userId) {
        likes.add(userId);
        rate++;
    }

    public void removeLike(int userId) {
        likes.remove(userId);
        rate--;
    }

    public void addGenre(Genre genreId) {
        genres.add(genreId);
    }

    public void removeGenre(Genre genreId) {
        genres.remove(genreId);
    }
}
