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

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenresRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Genre> getGenresByFilm(int id) {
        log.info("Получаем список жанров для фильма ID {}", id);
        String sql = "SELECT g.id AS genre_id, g.name AS genre_name " +
                "FROM films f " +
                "JOIN film_genres fg ON f.id = fg.film_id " +
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

    public List<Genre> setGenresToFilm(int filmId, List<Genre> genres) {
        List<Genre> result = new ArrayList<>();
        log.info("Вносим в таблицу жанры фильма ID {}", filmId);
        // Запрос для добавления жанра к фильму
        String insertSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

        for (Genre genre : genres) {
            // Проверяем, существует ли жанр в списке
            try {
                jdbcTemplate.update(insertSql, filmId, genre.getId());
                result.add(genre);
                log.info("Жанр с id: {} успешно добавлен фильму", genre.getId());
            } catch (DataIntegrityViolationException ex) {
                log.error("Жанра с ID " + genre.getId() + " не существует.");
            }
        }
        return result;
    }

    public void updateGenres(int filmId, List<Genre> genres) {
        // Удаляем старые жанры
        log.info("Удаляем все жанры фильма {}", filmId);
        try {
            String deleteSql = "DELETE FROM film_genres WHERE film_id = ?";
            jdbcTemplate.update(deleteSql, filmId);
            log.info("Жанры фильма с id:{} успешно удалены", filmId);
            setGenresToFilm(filmId, genres);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось удалить жанры");
        }
        setGenresToFilm(filmId, genres);
    }

    public void checkGenres(List<Genre> genres) {
        log.info("Проверяем, что список жанров не пуст");

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
        String sql = "SELECT id FROM genre WHERE id IN (" + placeholders + ")";

        // Выполняем запрос и получаем найденные id
        List<Integer> foundGenreIds = jdbcTemplate.queryForList(sql, Integer.class, genreIds.toArray());

        // Находим отсутствующие жанры
        Set<Integer> notFoundGenreIds = new HashSet<>(genreIds);
        notFoundGenreIds.removeAll(foundGenreIds);

        // Если есть отсутствующие жанры, выбрасываем исключение
        if (!notFoundGenreIds.isEmpty()) {
            log.warn("Не найдены жанры с ID: {}", notFoundGenreIds);
            throw new NotFoundException("Не найдены жанры с ID: " + notFoundGenreIds);
        }

        log.info("Все жанры найдены в базе данных");
    }

    public Genre getGenre(int id) {
        log.info("Получаем Жанр ID {}", id);
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
        log.info("Получаем список всех жанров");
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
}
