package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Тестирование класса InMemoryUserStorage")
class InMemoryUserStorageTest {

    private UserStorage userStorage;
    private User user;

    @BeforeEach
    void setUp() {
        userStorage = new InMemoryUserStorage();
        user = new User(0, "mail@mail.ru", "login", "User Name", LocalDate.of(2000, 12, 31), null);
    }

    @DisplayName("Тест: Добавить пользователя. Успешно")
    @Test
    void testAddUser_Success() {
        userStorage.addUser(user);

        assertEquals(1, userStorage.getUsers().size());
        User addedUser = userStorage.getUsers().get(0);
        assertEquals("mail@mail.ru", addedUser.getEmail());
        assertEquals("User Name", addedUser.getName());
    }

    @DisplayName("Тест: Удалить пользователя. Успешно")
    @Test
    void testDeleteUser_Success() {
        userStorage.addUser(user);
        int userId = user.getId();
        userStorage.deleteUser(userId);

        assertEquals(0, userStorage.getUsers().size());
    }

    @DisplayName("Тест: Удалить пользователя. Пользователь не найден")
    @Test
    void testDeleteUser_UserNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userStorage.deleteUser(2);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @DisplayName("Тест: Обновить пользователя. Успешно")
    @Test
    void testUpdateUser_Success() {
        userStorage.addUser(user);

        int userId = user.getId();

        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setLogin("newLogin");
        updatedUser.setName("New Name");

        userStorage.updateUser(updatedUser);

        User result = userStorage.getUser(userId);
        assertNotNull(result);
        assertEquals("newLogin", result.getLogin());
        assertEquals("New Name", result.getName());
    }

    @DisplayName("Тест: Обновить пользователя. Пользователь не найден")
    @Test
    void testUpdateUser_UserNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            User nonExistentUser = new User();
            nonExistentUser.setId(2);
            userStorage.updateUser(nonExistentUser);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @DisplayName("Тест: Получить пользователя. Успешно")
    @Test
    void testGetUser_Success() {
        userStorage.addUser(user);
        int userId = user.getId();
        User result = userStorage.getUser(userId);

        assertEquals("mail@mail.ru", result.getEmail());
        assertEquals("User Name", result.getName());
    }

    @DisplayName("Тест: Получить пользователя. Пользователь не найден")
    @Test
    void testGetUser_UserNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userStorage.getUser(2);
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @DisplayName("Тест: Получить пользователей")
    @Test
    void testGetUsers() {
        User user1 = new User(0, "mail1@mail.ru", "login1", "User Name 1", LocalDate.of(2010, 12, 31), null);
        userStorage.addUser(user);
        userStorage.addUser(user1);

        List<User> users = userStorage.getUsers();
        assertEquals(2, users.size());
    }
}