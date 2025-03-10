package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Map;

public interface UserStorage {
    Map<Integer, User> users = null;

    int generateId();

    void addUser(User user);

    void deleteUser(int id);

    void updateUser(User user);

    User getUser(int id);

    ArrayList<User> getUsers();
}
