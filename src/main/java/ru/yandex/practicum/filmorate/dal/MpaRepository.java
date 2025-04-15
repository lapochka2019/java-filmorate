package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.MpaRatingMapper;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class MpaRepository {

    private final JdbcTemplate jdbcTemplate;

    public MpaRating getMpa(int id) {
        log.info("Получаем Mpa с ID {}", id);
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

    public List<MpaRating> getAllMpa() {
        log.info("Получаем список всех Mpa");
        try {
            String sql = "SELECT id, name FROM mpa_rating";
            return jdbcTemplate.query(sql, new MpaRatingMapper());
        } catch (EmptyResultDataAccessException ex) {
            log.error("Не удалось получить МРА рейтинги");
            throw new NotFoundException("Не удалось получить МРА рейтинги");
        } catch (DataAccessException ex) {
            log.error("Во время получения МРА рейтингов произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить МРА рейтинги");
        }
    }
}
