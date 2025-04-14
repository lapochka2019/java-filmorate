package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class LikesRepository {

    private final JdbcTemplate jdbcTemplate;

    public List<Integer> setLikesToFilm(int filmId, List<Integer> likes) {
        // Запрос для добавления лайка
        String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";

        // Список для хранения успешно добавленных лайков
        List<Integer> successfulLikes = new ArrayList<>();

        for (Integer userId : likes) {
            try {
                // Проверяем, существует ли пользователь
                if (!userExists(userId)) {
                    log.error("Пользователя с id: {} нет в БД", userId);
                    continue; // Пропускаем этого пользователя
                }

                // Добавляем лайк
                jdbcTemplate.update(insertSql, filmId, userId);
                log.info("Лайк пользователя с id: {} успешно добавлен к фильму {}", userId, filmId);

                // Добавляем userId в список успешных лайков
                successfulLikes.add(userId);
            } catch (DataAccessException ex) {
                log.error("Ошибка при добавлении лайка пользователя с id: {} к фильму {}: {}", userId, filmId, ex.getMessage());
            }
        }

        return successfulLikes;
    }

    public void updateLikes(int filmId, List<Integer> likes) {
        // Удаляем старые лайки
        String deleteSql = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);
        log.info("Лайки фильма с id:{} успешно удалены", filmId);

        setLikesToFilm(filmId, likes);
    }

    public void addLike(int filmId, int userId) {
        // Проверка существования фильма
        if (!filmExists(filmId)) {
            log.error("Фильм с id {} не найден", filmId);
            throw new NotFoundException("Фильм с id " + filmId + " не найден");
        }

        // Проверка существования пользователя
        if (!userExists(userId)) {
            log.error("Пользователь с id {} не найден", userId);
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        }

        // Добавление лайка
        String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(insertSql, filmId, userId);
            log.info("Лайк пользователя с id: {} успешно добавлен к фильму {}", userId, filmId);
        } catch (DataAccessException ex) {
            log.error("Произошла ошибка при добавлении лайка. Возможно, дублирование лайка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Произошла ошибка при добавлении лайка. Возможно, дублирование лайка");
        }
    }

    // Метод для проверки существования фильма
    private boolean filmExists(int filmId) {
        String sql = "SELECT COUNT(*) FROM film WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);
        return count != null && count > 0;
    }

    // Метод для проверки существования пользователя
    private boolean userExists(int userId) {
        String sql = "SELECT COUNT(*) FROM consumer WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null && count > 0;
    }

    public void removeLike(int filmId, int userId) {
        String deleteSql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        try {
            jdbcTemplate.update(deleteSql, filmId, userId);
            log.info("Пользователя с id: {} успешно удален к фильму {}", userId, filmId);
        } catch (DataAccessException ex) {
            log.error("Произошла ошибка. Возможно не найден пользователь {}  или фильм {}", userId, filmId);
            throw new NotFoundException("Произошла ошибка. Возможно не найден пользователь " + userId + " или фильм " + filmId);
        }
    }

    public Set<Integer> getLikesForFilm(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }
}
