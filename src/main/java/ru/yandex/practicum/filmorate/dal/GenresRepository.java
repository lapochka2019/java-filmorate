package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.GenreMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenresRepository {

    private final JdbcTemplate jdbcTemplate;

    //получить жанры фильма
    public Set<Genre> getGenresByFilm(int id) {
        String sql = "SELECT g.id AS genre_id, g.name AS genre_name " +
                "FROM film f " +
                "JOIN film_genre fg ON f.id = fg.film_id " +
                "JOIN genre g ON fg.genre_id = g.id " +
                "WHERE f.id = ?";

        try {
            List<Genre> genres = jdbcTemplate.query(sql, new GenreMapper(), id);
            return new HashSet<>(genres); // Преобразуем список в множество
        } catch (EmptyResultDataAccessException ex) {
            log.warn("Жанры для фильма с ID {} не найдены", id);
            return Collections.emptySet(); // Возвращаем пустое множество
        } catch (DataAccessException ex) {
            log.error("Во время получения жанров произошла непредвиденная ошибка: {}", ex.getMessage(), ex);
            throw new DataIntegrityViolationException("Не удалось получить жанры");
        }
    }

    // Добавление жанров к фильму
    public Set<Genre> setGenresToFilm(int filmId, Set<Genre> genres) {
        // Запрос для добавления жанра к фильму
        String insertSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : genres) {
            // Проверяем, существует ли жанр в списке
            try {
                jdbcTemplate.update(insertSql, filmId, genre.getId());
                log.info("Жанр с id: {} успешно добавлен фильму", genre.getId());
            } catch (DataIntegrityViolationException ex) {
                genres.remove(genre.getId());
                log.error("Жанра с ID " + genre.getId() + " не существует.");
            }
        }
        return genres;
    }

    public void updateGenres(int filmId, Set<Genre> genres) {
        // Удаляем старые жанры
        try {
            String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
            jdbcTemplate.update(deleteSql, filmId);
            log.info("Жанры фильма с id:{} успешно удалены", filmId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось удалить жанры");
        }
        setGenresToFilm(filmId, genres);
    }

    public Genre getGenre(int id) {
        try {
            String sql = "SELECT * FROM genre WHERE id=?";
            return jdbcTemplate.queryForObject(sql, new GenreMapper(), id);
        } catch (EmptyResultDataAccessException ex) {
            log.error("Не удалось получить жанр");
            throw new NotFoundException("Жанр с ID " + id + " не найден");
        } catch (DataAccessException ex) {
            log.error("Во время получения жанра произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить жанр");
        }
    }

    public List<Genre> getGenres() {
        try {
            String sql = "SELECT id,name FROM genre";
            return jdbcTemplate.query(sql, new GenreMapper());
        } catch (EmptyResultDataAccessException ex) {
            log.error("Не удалось получить жанры");
            throw new NotFoundException("Не удалось получить жанры");
        } catch (DataAccessException ex) {
            log.error("Во время получения жанров произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить жанры");
        }
    }

//    public List<FilmDto> getFilmsWithGenre() {
//        try {
//            String sql = "SELECT f.id, " +
//                    "       f.name, " +
//                    "       f.description, " +
//                    "       f.release_date, " +
//                    "       f.duration, " +
//                    "       f.rate, " +
//                    "       m.name AS mpa_name, " +
//                    "       LISTAGG(DISTINCT fl.user_id, ',') AS likes, " +
//                    "       LISTAGG(DISTINCT g.name, ',') AS genres " +
//                    "FROM film f " +
//                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
//                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
//                    "INNER JOIN film_genre fg ON f.id = fg.film_id " + // Только фильмы с жанрами
//                    "LEFT JOIN genre g ON fg.genre_id = g.id " +
//                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rate, m.name";
//
//            // Выполняем запрос и маппинг результатов
//            return jdbcTemplate.query(sql, new FilmDtoMapper());
//        } catch (EmptyResultDataAccessException ex) {
//            log.warn("Нет фильмов с жанрами в БД");
//            return Collections.emptyList(); // Возвращаем пустой список, если данных нет
//        } catch (DataAccessException ex) {
//            log.error("Ошибка при получении фильмов с жанрами: {}", ex.getMessage());
//            throw new NotFoundException("Не удалось получить фильмы с жанрами");
//        }
}
