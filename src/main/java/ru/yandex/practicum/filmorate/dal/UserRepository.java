package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.UserDtoMapper;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;
    private final FriendshipRepository friendshipRepository;

    //Добавить
    public void addUser(User user) {

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, user.getEmail());
                ps.setString(2, user.getLogin());
                ps.setString(3, user.getName());
                ps.setDate(4, java.sql.Date.valueOf(user.getBirthday()));
                return ps;
            }, keyHolder);

            int userId = keyHolder.getKey().intValue();
            user.setId(userId);
            log.info("Выполнено добавление нового пользователя в БД. ID пользователя: {}", userId);
            log.info(user.toString());
            // Добавляем друзей
            log.info("Вызван метод добавления друзей пользователя в БД");
            friendshipRepository.setFriends(userId, user.getFriends());

        } catch (DataIntegrityViolationException ex) {
            log.error("Во время добавления пользователя в БД произошла непредвиденная ошибка");
            throw new DataIntegrityViolationException("Не удалось добавить пользователя в базу данных.");
        }
    }

    //Редактировать
    public void updateUser(User user) {
        log.info("Выполняется обновление пользователя с ID пользователя: {}", user.getId());
        // Проверяем, существует ли пользователь в БД
        checkUserExists(user.getId());

        // SQL-запрос для обновления данных пользователя
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        try {
            jdbcTemplate.update(sql,
                    user.getEmail(),
                    user.getLogin(),
                    user.getName(),
                    java.sql.Date.valueOf(user.getBirthday()),
                    user.getId());

            log.info("Выполнено обновление данных пользователя с ID: {}", user.getId());

            // Добавляем друзей
            log.info("Вызван метод обновления друзей пользователя в БД");
            friendshipRepository.updateFriends(user.getId(), user.getFriends());
        } catch (DataIntegrityViolationException ex) {
            log.error("Во время обновления данных пользователя с ID {} произошла ошибка", user.getId());
            throw new DataIntegrityViolationException("Не удалось обновить данные пользователя.");
        }
    }

    // Метод для проверки существования пользователя в БД
    public void checkUserExists(int userId) {
        String sql = "SELECT COUNT(*) FROM users WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        if (count == null || count == 0) {
            log.error("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
    }

    //Получить (1)
    public UserDto getUser(int id) {
        log.info("Пытаемся получить данные пользователя ID {}", id);
        String sql = "SELECT id, email, login, name, birthday FROM users WHERE id = ?";
        try {
            UserDto user = jdbcTemplate.queryForObject(sql, new UserDtoMapper(), id);
            return user;
        } catch (EmptyResultDataAccessException ex) {
            log.error("Пользователь с ID {} не найден", id);
            throw new NotFoundException("Пользователь с ID " + id + " не найден");
        } catch (DataAccessException ex) {
            log.error("Во время получения пользователя произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить пользователя");
        }
    }

    //Получить всех
    public List<UserDto> getUsers() {
        log.info("Пытаемся получить данные всех пользователей");
        String sql = "SELECT id, email, login, name, birthday FROM users";
        try {
            // Получаем всех пользователей
            List<UserDto> users = jdbcTemplate.query(sql, new UserDtoMapper());
            log.info("Успешно загружено {} пользователей.", users.size());
            return users;
        } catch (NotFoundException ex) {
            log.error("Во время получения списка пользователей произошла ошибка", ex);
            throw new NotFoundException("Не удалось получить список пользователей.");
        }
    }

    //Отправить запрос в друзья
    public void addFriend(int firstId, int secondId) {
        log.info("Пытаемся добавить пользователя {} в друзья к пользователю {}", secondId, firstId);
        // Проверяем, существует ли пользователь в БД
        checkUserExists(firstId);
        // Проверяем, существует ли пользователь в БД
        checkUserExists(secondId);
        //проверяем, есть ли уже запрос в друзья наоборот (2 к 1)
        Optional<Integer> friendship = friendshipRepository.getFriendshipType(secondId, firstId);
        //если есть, то подтверждаем дружбу
        if (friendship.isPresent()) {
            log.info("Пользователь {} уже отправлял запрос в друзья к пользователю {}", firstId, secondId);
            friendshipRepository.confirmFriendship(secondId, firstId);
        }
        //если нет, то создаем
        friendshipRepository.addFriend(firstId, secondId);
    }

    //Удалить из друзей
    public void deleteFriend(int firstId, int secondId) {
        log.info("Пользователь {} хочет удалить из друзей пользователя {}", firstId, secondId);
        // Проверяем, существует ли пользователь в БД
        checkUserExists(firstId);
        // Проверяем, существует ли пользователь в БД
        checkUserExists(secondId);
        //получаем тип дружбы
        Optional<Integer> friendshipOpt = friendshipRepository.getFriendshipType(firstId, secondId);
        //если дружба есть
        if (friendshipOpt.isPresent()) {
            int friendship = friendshipOpt.get();
            //Если дружба не подтверждена
            if (friendship == 1) {
                log.info("Пользователь {} отменяет свою заявку в друзья к пользователю {}", firstId, secondId);
                //то удаляем
                friendshipRepository.removeFriend(firstId, secondId);
            } else {
                //Если дружба подтверждена
                log.info("Пользователь {} отменяет отменяет подтверждение дружбы с пользователем {}", firstId, secondId);
                //Удаляем
                friendshipRepository.removeFriend(firstId, secondId);
                //Возвращаем заявку в друзья
                friendshipRepository.addFriend(secondId, firstId);
            }
        } else {
            log.error("Пользователи {} и {} не являются друзьями", firstId, secondId);
            //throw new NotFoundException("Данные пользователи не являются друзьями");
        }
    }

    //Получить друзей
    public List<UserDto> getFriends(int id) {
        log.info("Получаем список всех друзей пользователя {}", id);
        checkUserExists(id);
        List<UserDto> friendsList = new ArrayList<>();
        Set<Integer> friends = friendshipRepository.getFriendsForUser(id);
        for (Integer userId : friends) {
            friendsList.add(getUser(userId));
        }
        return friendsList;
    }

    //Получить общих друзей
    public List<UserDto> getMutualFriends(int firstId, int secondId) {
        log.info("Получаем список общих друзей пользователей {} и {}", secondId, firstId);
        // Проверяем, существует ли пользователь в БД
        checkUserExists(firstId);
        // Проверяем, существует ли пользователь в БД
        checkUserExists(secondId);
        List<UserDto> friendsList = new ArrayList<>();
        Set<Integer> friends = friendshipRepository.getCommonFriends(firstId, secondId);
        for (Integer userId : friends) {
            friendsList.add(getUser(userId));
        }
        return friendsList;
    }
}
