package ru.yandex.practicum.filmorate.service;

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

@DisplayName("Тестирование класса UserService")
public class UserServiceTest {

    private UserStorage userStorage;
    private UserService userService;

    @BeforeEach
    void setUp() {
        // Инициализация реального хранилища пользователей
        userStorage = new InMemoryUserStorage();
        userService = new UserService(userStorage);

        // Добавление начальных данных
        User user1 = new User(0, "mail1@mail.ru", "login1", "User Name1", LocalDate.of(1975, 12, 31), null);
        User user2 = new User(0, "mail2@mail.ru", "login2", "User Name2", LocalDate.of(1999, 12, 31), null);
        User user3 = new User(0, "mail3@mail.ru", "login3", "User Name3", LocalDate.of(2003, 12, 31), null);
        userStorage.addUser(user1);
        userStorage.addUser(user2);
        userStorage.addUser(user3);
    }

    @DisplayName("Тест: Добавить пользователя в друзья. Успешно")
    @Test
    void testAddFriend_Success() {
        userService.addFriend(1, 2);

        User user1 = userStorage.getUser(1);
        User user2 = userStorage.getUser(2);

        assertTrue(user1.getFriends().contains(2));
        assertTrue(user2.getFriends().contains(1));
    }

    @DisplayName("Тест: Добавить пользователя в друзья. Пользователь не найден")
    @Test
    void testAddFriend_UserNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.addFriend(1, 999); // Несуществующий пользователь
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @DisplayName("Тест: Удалить пользователей из друзей. Успешно")
    @Test
    void testDeleteFriend_Success() {
        userService.addFriend(1, 2); // Добавляем дружбу

        userService.deleteFriend(1, 2);

        User user1 = userStorage.getUser(1);
        User user2 = userStorage.getUser(2);

        assertFalse(user1.getFriends().contains(2));
        assertFalse(user2.getFriends().contains(1));
    }

    @DisplayName("Тест: Удалить пользователей из друзей. Пользователь не найден")
    @Test
    void testDeleteFriend_UserNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.deleteFriend(1, 999); // Несуществующий пользователь
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @DisplayName("Тест: Получить друзей. Успешно")
    @Test
    void testGetFriends_Success() {
        userService.addFriend(1, 2);
        userService.addFriend(1, 3);

        List<User> friends = userService.getFriends(1);

        assertNotNull(friends);
        assertEquals(2, friends.size());
    }

    @DisplayName("Тест: Получить друзей. Пользователь не найден")
    @Test
    void testGetFriends_UserNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getFriends(999); // Несуществующий пользователь
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }

    @DisplayName("Тест: Получить общих друзей. Успешно")
    @Test
    void testGetMutualFriends_Success() {
        userService.addFriend(1, 2); // Пользователь 1 и 2 становятся друзьями
        userService.addFriend(1, 3); // Пользователь 1 и 3 становятся друзьями
        userService.addFriend(2, 3); // Пользователь 2 и 3 становятся друзьями

        List<User> mutualFriends = userService.getMutualFriends(1, 2);

        assertNotNull(mutualFriends);
        assertEquals(1, mutualFriends.size());
    }

    @DisplayName("Тест: Получить общих друзей. Нет общих друзей")
    @Test
    void testGetMutualFriends_NoMutualFriends() {
        User user4 = new User(0, "mail4@mail.ru", "login4", "User Name4", LocalDate.of(2004, 12, 31), null);
        userStorage.addUser(user4);

        userService.addFriend(1, 2); // Пользователь 1 и 2 становятся друзьями
        userService.addFriend(3, 4); // Пользователь 3 и 4 становятся друзьями

        List<User> mutualFriends = userService.getMutualFriends(1, 3);

        assertNotNull(mutualFriends);
        assertTrue(mutualFriends.isEmpty());
    }

    @DisplayName("Тест: Получить общих друзей. Пользователь не найден")
    @Test
    void testGetMutualFriends_UserNotFound() {
        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            userService.getMutualFriends(1, 999); // Несуществующий пользователь
        });

        assertEquals("Пользователь не найден", exception.getMessage());
    }
}
