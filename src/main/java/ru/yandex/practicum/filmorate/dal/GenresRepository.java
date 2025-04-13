package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GenresRepository {

    private final JdbcTemplate jdbcTemplate;

    // Добавление жанров к фильму
    public void setGenresToFilm(int filmId, Set<Integer> genres) {
        // Запрос для добавления жанра к фильму
        String insertSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

        for (Integer genreId : genres) {
            // Проверяем, существует ли жанр в списке
            try {
                jdbcTemplate.update(insertSql, filmId, genreId);
                log.info("Жанр с id: {} успешно добавлен фильму", genreId);
            } catch (DataIntegrityViolationException ex) {
                genres.remove(genreId);
                log.error("Жанра с ID " + genreId + " не существует.");
            }
        }
    }

    public void updateGenres(int filmId, Set<Integer> genres) {
        // Удаляем старые жанры
        String deleteSql = "DELETE FROM film_genre WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);
        log.info("Жанры фильма с id:{} успешно удалены", filmId);

        // Добавляем новые жанры
        if (!genres.isEmpty()) {
            String insertSql = "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";
            for (Integer genreId : genres) {
                try {
                    jdbcTemplate.update(insertSql, filmId, genreId);
                    log.info("Жанр с id: {} успешно добавлен фильму", genreId);
                } catch (DataIntegrityViolationException ex) {
                    genres.remove(genreId);
                    log.error("Жанра с ID " + genreId + " не существует.");
                }
            }
        }
    }

    public Set<Integer> getGenresForFilm(int filmId) {
        String sql = "SELECT genre_id FROM film_genre WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }

}
