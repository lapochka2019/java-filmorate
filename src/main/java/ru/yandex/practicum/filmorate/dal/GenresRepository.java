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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenresRepository {

    private final JdbcTemplate jdbcTemplate;

    //получить жанры фильма
    public List<Genre> getGenresByFilm(int id) {
        String sql = "SELECT g.id AS genre_id, g.name AS genre_name " +
                "FROM film f " +
                "JOIN film_genre fg ON f.id = fg.film_id " +
                "JOIN genre g ON fg.genre_id = g.id " +
                "WHERE f.id = ?";

        try {
            return jdbcTemplate.query(sql, new GenreMapper(), id); // Преобразуем список в множество
        } catch (EmptyResultDataAccessException ex) {
            log.warn("Жанры для фильма с ID {} не найдены", id);
            return new ArrayList<>(); // Возвращаем пустое множество
        } catch (DataAccessException ex) {
            log.error("Во время получения жанров произошла непредвиденная ошибка: {}", ex.getMessage(), ex);
            throw new DataIntegrityViolationException("Не удалось получить жанры");
        }
    }

    // Добавление жанров к фильму
    public List<Genre> setGenresToFilm(int filmId, List<Genre> genres) {
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

    public void updateGenres(int filmId, List<Genre> genres) {
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

    public void checkGenre(List<Genre> genres) {
        // Проверяем, что список жанров не пуст
        if (genres == null || genres.isEmpty()) {
            log.warn("Список жанров пуст");
            return;
        }

        // Извлекаем id из объектов Genre
        Set<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        // Формируем строку плейсхолдеров
        String placeholders = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String sql = "SELECT COUNT(*) FROM genre WHERE id IN (" + placeholders + ")";

        // Выполняем запрос
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreIds.toArray());

        // Проверяем количество найденных жанров
        if (count == null || count < genreIds.size()) {
            log.warn("Не все жанры с ID {} найдены в базе данных", genreIds);
            throw new NotFoundException("Не все жанры найдены в базе данных");
        }

        log.info("Все жанры с ID {} найдены в базе данных", genreIds);
    }
}
