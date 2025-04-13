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
public class LikesRepository {

    private final JdbcTemplate jdbcTemplate;

    public void setLikesToFilm(int filmId, Set<Integer> likes) {
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

    public void updateLikes(int filmId, Set<Integer> likes) {
        // Удаляем старые лайки
        String deleteSql = "DELETE FROM film_likes WHERE film_id = ?";
        jdbcTemplate.update(deleteSql, filmId);
        log.info("Лайки фильма с id:{} успешно удалены", filmId);

        // Добавляем новые лайки
        if (!likes.isEmpty()) {
            String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
            for (Integer userId : likes) {
                //если существует пользователь с таким айди, то добавляем
                try {
                    jdbcTemplate.update(insertSql, filmId, userId);
                    log.info("Пользователя с id: {} успешно добавлен к фильму {}", userId, filmId);
                } catch (DataIntegrityViolationException ex) {
                    log.error("Пользователя с id: {} нет в БД", userId);
                    likes.remove(userId);
                }
            }
        }
    }

    public void addLike(int filmId, int userId) {
        String insertSql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(insertSql, filmId, userId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Пользователя с id: {} нет в БД", userId);
        }
    }

    public void removeLike(int filmId, int userId) {
        String deleteSql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        try {
            jdbcTemplate.update(deleteSql, filmId, userId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось удалить лайк");
        }
    }

    public Set<Integer> getLikesForFilm(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }


}
