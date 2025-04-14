package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmDtoMapper;
import ru.yandex.practicum.filmorate.mappers.MpaRatingMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaRepository {

    private final JdbcTemplate jdbcTemplate;

    public MpaRating getMpa(int id) {
        try {
            String sql = "SELECT id, name FROM mpa_rating WHERE id =?";
            return jdbcTemplate.queryForObject(sql, new MpaRatingMapper(), id);
        } catch (EmptyResultDataAccessException ex) {
            log.error("Не удалось получить МРА рейтинг");
            throw new NotFoundException("MPA с ID " + id + " не найден");
        } catch (DataAccessException ex) {
            log.error("Во время получения МРА рейтинга произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить МРА рейтинг");
        }
    }

    public List<FilmDto> getFilmsWithMpa() {
        try {
            String sql = "SELECT f.id, " +
                    "       f.name, " +
                    "       f.description, " +
                    "       f.release_date, " +
                    "       f.duration, " +
                    "       f.rate, " +
                    "       m.name AS mpa_name, " +
                    "       LISTAGG(DISTINCT fl.user_id, ',') AS likes, " +
                    "       LISTAGG(DISTINCT g.name, ',') AS genres " +
                    "FROM film f " +
                    "INNER JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genre fg ON f.id = fg.film_id " +
                    "LEFT JOIN genre g ON fg.genre_id = g.id " +
                    "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.rate, m.name";

            log.info("Выполняется запрос на получение фильмов с MPA рейтингом");
            List<FilmDto> films = jdbcTemplate.query(sql, new FilmDtoMapper());

            if (films.isEmpty()) {
                log.warn("Список фильмов с MPA рейтингом пуст");
            } else {
                log.info("Успешно получено {} фильмов с MPA рейтингом", films.size());
            }

            return films;
        } catch (EmptyResultDataAccessException ex) {
            log.warn("Нет фильмов с MPA рейтингом в БД");
            return Collections.emptyList(); // Возвращаем пустой список, если данных нет
        } catch (DataAccessException ex) {
            log.error("Ошибка при получении фильмов с MPA рейтингом: {}", ex.getMessage());
            throw new NotFoundException("Не удалось получить фильмы с MPA рейтингом");
        }
    }
}
