package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Set;

import static java.sql.Types.NULL;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmRepository {

    private final JdbcTemplate jdbcTemplate;
    private final FilmMapper filmMapper;

    //Создать
    public void addFilm(Film film) {
        //Проверяем возрастной рейтинг
        if (!checkMpaRating(film.getMpa())) {
            film.setMpa(NULL);
            log.error("Возрастное ограничение с id: {} не найдено", film.getMpa());
        }

        String sql = "INSERT INTO film (name, description, release_date, duration, rate, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // Создаем KeyHolder для получения сгенерированного ключа
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            // Выполняем запрос с указанием RETURN_GENERATED_KEYS
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
                ps.setInt(4, film.getDuration());
                ps.setInt(5, film.getRate());
                ps.setInt(6, film.getMpa()); // Предполагается, что getMpa() возвращает ID рейтинга
                return ps;
            }, keyHolder);
            // Получаем последний вставленный id
            int filmId = keyHolder.getKey().intValue();

            log.info("Выполнено добавление нового фильма в БД. ID фильма: {}", filmId);

            // Добавляем жанры
            log.info("Вызван метод добавления жанров фильма в БД");
            addGenresToFilm(filmId, film.getGenres());

            // Добавляем лайки
            log.info("Вызван метод добавления лайков фильма в БД");
            addLikesToFilm(filmId, film.getLikes());

            //проверка на значение поля Рейтинг
            if(film.getRate()!=film.getLikes().size()){
                updateFilmRate(filmId, film.getLikes().size());
            }
        } catch (DataIntegrityViolationException ex) {
            log.error("Во время добавления фильма в БД произошла непредвиденная ошибка. Жанры и Лайки не должны быть добавлены");
            throw new DataIntegrityViolationException("Не удалось добавить фильм в базу данных.");
        }
    }
    //Обновить
    public void updateFilm(Film film){

    }
    //Получить (1) + жанры + лайки
    //Получить (список) + жанры + лайки
    //Поставить лайк
    //Удалить лайк
    //Получить топ (10) фильмов

    // Добавление жанров к фильму
    private void addGenresToFilm(int filmId, Set<Integer> genres) {
        // Запрос для добавления жанра к фильму
        String insertGenreSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        for (Integer genreId : genres) {
            // Проверяем, существует ли жанр в списке
            try {
                jdbcTemplate.update(insertGenreSql, filmId, genreId);
                log.info("Жанр с id: {} успешно добавлен фильму", genreId);
            } catch (DataIntegrityViolationException ex) {
                genres.remove(genreId);
                log.error("Жанр с ID " + genreId + " не существует.");
            }
        }
    }

    // Добавление лайков к фильму
    private void addLikesToFilm(int filmId, Set<Integer> likes) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        for (Integer userId : likes) {
            //если существует пользователь с таким айди, то добавляем
            try {
                jdbcTemplate.update(sql, filmId, userId);
                log.info("Пользователя с id: {} успешно добавлен к фильму {}", userId, filmId);
            } catch (DataIntegrityViolationException ex) {
                log.error("Пользователя с id: {} нет в БД", userId);
                likes.remove(userId);
            }

        }
    }

    //Проверяем, есть ли указанный возрастной рейтинг в таблице
    private boolean checkMpaRating(int ratingId) {
        String sql = "SELECT id FROM mpa_rating";
        List<Integer> mpaRatingList = jdbcTemplate.queryForList(sql, Integer.class);
        return mpaRatingList.contains(ratingId);
    }

    private void updateFilmRate(int filmId, int rate){
        String sql = "UPDATE film SET rate=? WHERE id=?";
        jdbcTemplate.update(sql, rate,filmId);
        log.info("Значение поля rate актуализировано");
    }
}
