package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.*;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FriendshipRepository {

    private final JdbcTemplate jdbcTemplate;

    public void setFriends(int userId, Set<Integer> friendIds) {
        for (int friendId : friendIds) {
            addFriend(userId, friendId);
        }
    }

    public void updateFriends(int userId, Set<Integer> friendIds) {
        for (int friendId : friendIds) {
            removeFriend(userId, friendId);
        }
        setFriends(userId, friendIds);
    }

    public void addFriend(int userId, int friendId) {
        // SQL-запрос для добавления дружбы
        String sql = "INSERT INTO user_friends (user_id, friend_id, friendship_type_id) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, userId, friendId, 1);
            log.info("Запрос дружбы пользователя {} пользователю {} отправлен успешно", userId, friendId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось отправить запрос дружбы пользователя {} пользователю {}: {}", userId, friendId, ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось отправить запрос дружбы");
        }
    }

    public void removeFriend(int userId, int friendId) {
        //Если тип дружбы 1, то удаляем, если тип дружбы два, то ставим 1
        String sql = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";
        try {
            jdbcTemplate.update(sql, userId, friendId);
            log.info("Пользователь {} больше не является другом пользователя {}", userId, friendId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось разорвать дружбу пользователя {} с пользователем {}", userId, friendId);
        }
    }

    public void confirmFriendship(int userId, int friendId) {
        String sql = "UPDATE user_friends SET friendship_type_id = ? WHERE user_id = ? AND friend_id = ?";
        try {
            jdbcTemplate.update(sql, 2, userId, friendId);
            log.info("Пользователь {} подтвердил дружбу с пользователем {}", userId, friendId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось подтвердить дружбу между пользователями {} и {}", userId, friendId);
        }
    }

    public Set<Integer> getFriendsForUser(int userId) {

        try {
            String sql = "SELECT friend_id AS friend FROM user_friends WHERE user_id = ? " +
                    "UNION " +
                    "SELECT user_id AS friend FROM user_friends WHERE friend_id = ? AND friendship_type_id=2";

            // Выполняем запрос и получаем список ID друзей
            List<Integer> friends = jdbcTemplate.queryForList(sql, Integer.class, userId, userId);

            log.info("Успешно получены друзья пользователя {}", userId);
            return new HashSet<>(friends); // Преобразуем список в множество
        } catch (EmptyResultDataAccessException ex) {
            log.warn("У пользователя {} нет друзей", userId);
            return Collections.emptySet(); // Возвращаем пустое множество, если друзей нет
        } catch (DataAccessException ex) {
            log.error("Ошибка при получении списка друзей пользователя {}: {}", userId, ex.getMessage());
            throw new NotFoundException("Не удалось получить список друзей пользователя " + userId);
        }
    }

    public Set<Integer> getCommonFriends(int userId1, int userId2) {
        try {
            String sql = "SELECT DISTINCT uf.friend " +
                    "FROM (" +
                    "    SELECT friend_id AS friend FROM user_friends WHERE user_id = ? " +
                    "    UNION " +
                    "    SELECT user_id AS friend FROM user_friends WHERE friend_id = ? " +
                    ") uf " +
                    "JOIN (" +
                    "    SELECT friend_id AS friend FROM user_friends WHERE user_id = ? " +
                    "    UNION " +
                    "    SELECT user_id AS friend FROM user_friends WHERE friend_id = ? " +
                    ") uf2 ON uf.friend = uf2.friend";

            // Выполняем запрос и получаем список ID общих друзей
            List<Integer> commonFriends = jdbcTemplate.queryForList(sql, Integer.class, userId1, userId1, userId2, userId2);

            log.info("Успешно получены общие друзья пользователей {} и {}", userId1, userId2);
            return new HashSet<>(commonFriends); // Преобразуем список в множество
        } catch (EmptyResultDataAccessException ex) {
            log.warn("У пользователей {} и {} нет общих друзей", userId1, userId2);
            return Collections.emptySet(); // Возвращаем пустое множество, если общих друзей нет
        } catch (DataAccessException ex) {
            log.error("Ошибка при получении общих друзей пользователей {} и {}: {}", userId1, userId2, ex.getMessage());
            throw new NotFoundException("Не удалось получить список общих друзей пользователей " + userId1 + " и " + userId2);
        }
    }

    public Optional<Integer> getFriendshipType(int userId, int friendId) {
        String sql = "SELECT friendship_type_id FROM user_friends WHERE user_id = ? AND friend_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Integer.class, userId, friendId));
        } catch (Exception ex) {
            log.warn("Запись о дружбе между пользователем {} и пользователем {} не найдена.", userId, friendId);
            return Optional.empty();
        }
    }
}
