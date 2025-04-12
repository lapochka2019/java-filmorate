//package ru.yandex.practicum.filmorate.service;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.model.User;
//import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
//import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
//import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;
//import ru.yandex.practicum.filmorate.storage.user.UserStorage;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DisplayName("Тестирование класса FilmService")
//public class FilmServiceTest {
//
//    private FilmStorage filmStorage;
//    private UserStorage userStorage;
//    private FilmService filmService;
//
//    @BeforeEach
//    void setUp() {
//        filmStorage = new InMemoryFilmStorage();
//        userStorage = new InMemoryUserStorage();
//        filmService = new FilmService(filmStorage, userStorage);
//
//        // Добавление начальных данных
//        User user1 = new User(0, "mail@mail.ru", "login1", "User Name1", LocalDate.of(1975, 12, 31), null);
//        User user2 = new User(0, "mail1@mail.ru", "login2", "User Name2", LocalDate.of(1999, 12, 31), null);
//        userStorage.addUser(user1);
//        userStorage.addUser(user2);
//
//        Film film1 = new Film(0, "Film name 1", "description 1", LocalDate.of(2000, 12, 31), 100, null);
//        Film film2 = new Film(0, "Film name 2", "description 2", LocalDate.of(2002, 12, 31), 100, null);
//        filmStorage.addFilm(film1);
//        filmStorage.addFilm(film2);
//    }
//
//    @DisplayName("Тест: Поставить лайк фильму. Успешно")
//    @Test
//    void testAddLike_Success() {
//        filmService.addLike(1, 1); // Пользователь 1 ставит лайк фильму 1
//
//        Film film = filmStorage.getFilm(1);
//        assertNotNull(film);
//        assertTrue(film.getLikes().contains(1));
//    }
//
//    @DisplayName("Тест: Поставить лайк фильму. Пользователь не найден")
//    @Test
//    void testAddLike_UserNotFound() {
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            filmService.addLike(99, 1); // Несуществующий пользователь
//        });
//
//        assertEquals("Пользователь не найден", exception.getMessage());
//    }
//
//    @DisplayName("Тест: Поставить лайк фильму. Фильм не найден")
//    @Test
//    void testAddLike_FilmNotFound() {
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            filmService.addLike(1, 99); // Несуществующий фильм
//        });
//
//        assertEquals("Данный фильм не найден", exception.getMessage());
//    }
//
//    @DisplayName("Тест: Удалить лайк. Успешно")
//    @Test
//    void testRemoveLike_Success() {
//        filmService.addLike(1, 1); // Пользователь 1 ставит лайк фильму 1
//
//        filmService.deleteLike(1, 1); // Пользователь 1 удаляет лайк с фильма 1
//
//        Film film = filmStorage.getFilm(1);
//        assertNotNull(film);
//        assertFalse(film.getLikes().contains(1));
//    }
//
//    @DisplayName("Тест: Удалить лайк. Фильм не найден")
//    @Test
//    void testRemoveLike_FilmNotFound() {
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            filmService.deleteLike(1, 99); // Несуществующий фильм
//        });
//
//        assertEquals("Данный фильм не найден", exception.getMessage());
//    }
//
//    @DisplayName("Тест: Удалить лайк. Пользователь не найден")
//    @Test
//    void testRemoveLike_UserNotFound() {
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            filmService.deleteLike(99, 1); // Несуществующий фильм
//        });
//
//        assertEquals("Пользователь не найден", exception.getMessage());
//    }
//
//    @DisplayName("Тест: Получить топ фильмов")
//    @Test
//    void testGetTopFilms() {
//        filmService.addLike(1, 1); // Пользователь 1 ставит лайк фильму 1
//        filmService.addLike(2, 1); // Пользователь 2 ставит лайк фильму 1
//        filmService.addLike(1, 2); // Пользователь 1 ставит лайк фильму 2
//
//        List<Film> topFilms = filmService.getTopFilms(2);
//
//        assertEquals(2, topFilms.size());
//        assertEquals("Film name 1", topFilms.get(0).getName()); // Фильм 1 должен быть первым (2 лайка)
//        assertEquals("Film name 2", topFilms.get(1).getName()); // Фильм 2 должен быть вторым (1 лайк)
//    }
//}
