package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    protected Map<Integer, User> users;
    protected int idCounter = 0;

    public InMemoryUserStorage() {
        users = new HashMap<>();
    }

    @Override
    public int generateId() {
        return ++idCounter;
    }

    @Override
    public void addUser(User user) {
        user.setId(generateId());
        putUserName(user);
        putUserFriends(user);
        users.put(idCounter, user);
        log.info("Успешно создан новый пользователь: {}", user);
    }

    @Override
    public void deleteUser(int id) {
        log.info("Попытка удалить пользователя: {}", id);
        if (users.containsKey(id)) {
            users.remove(id);
            log.info("Пользователь успешно удален");
        } else {
            log.warn("Попытка удалить несуществующего пользователя");
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public void updateUser(User user) {
        log.info("Попытка обновить фильм: {}", user);
        if (users.containsKey(user.getId())) {
            putUserName(user);
            putUserFriends(user);
            users.put(user.getId(), user);
            log.info("Пользователь успешно обновлен");
        } else {
            log.warn("Попытка обновить несуществующего пользователя");
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public User getUser(int id) {
        if (users.containsKey(id)) {
            log.info("Пользователь успешно получен");
            return users.get(id);
        } else {
            log.warn("Попытка получить несуществующего пользователя");
            throw new NotFoundException("Пользователь не найден");
        }
    }

    @Override
    public ArrayList<User> getUsers() {
        log.info("Успешно получен список пользователей");
        return new ArrayList<>(users.values());
    }

    //заполнить имя пользователя
    private void putUserName(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя заполнено, как Логин");
        }
    }

    //заполнить друзей пользователя
    private void putUserFriends(User user) {
        if (user.getFriends() == null) {
            user.setFriends(new HashSet<>());
            log.info("Друзья пользователя заполнены, как пустой HashSet");
        }
    }
}
