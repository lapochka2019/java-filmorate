//package ru.yandex.practicum.filmorate.storage;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import ru.yandex.practicum.filmorate.exception.NotFoundException;
//import ru.yandex.practicum.filmorate.model.Film;
//import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
//import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
//
//import java.time.LocalDate;
//import java.util.List;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//@DisplayName("Тестирование класса InMemoryFilmStorage")
//public class InMemoryFilmStorageTest {
//
//    private FilmStorage filmStorage;
//    private Film film;
//
//    @BeforeEach
//    void setUp() {
//        filmStorage = new InMemoryFilmStorage();
//        film = new Film(0, "Film name", "description", LocalDate.of(2000, 12, 31), 100, null);
//    }
//
//    @DisplayName("Тест: Добавить фильм. Успешно")
//    @Test
//    void testAddFilm_Success() {
//        filmStorage.addFilm(film);
//
//        assertEquals(1, filmStorage.getFilms().size());
//        Film addedFilm = filmStorage.getFilms().get(0);
//        assertNotNull(addedFilm.getId());
//        assertEquals("Film name", addedFilm.getName());
//    }
//
//    @DisplayName("Тест: Удалить фильм. Успешно")
//    @Test
//    void testDeleteFilm_Success() {
//        filmStorage.addFilm(film);
//
//        int filmId = film.getId();
//
//        filmStorage.deleteFilm(filmId);
//
//        assertEquals(0, filmStorage.getFilms().size());
//    }
//
//    @DisplayName("Тест: Удалить фильм. Фильм не найден")
//    @Test
//    void testDeleteFilm_FilmNotFound() {
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            filmStorage.deleteFilm(2);
//        });
//
//        assertEquals("Данный фильм не найден", exception.getMessage());
//    }
//
//    @DisplayName("Тест: Обновить фильм. Успешно")
//    @Test
//    void testUpdateFilm_Success() {
//        filmStorage.addFilm(film);
//
//        int filmId = film.getId();
//
//        Film updatedFilm = new Film();
//        updatedFilm.setId(filmId);
//        updatedFilm.setName("New Name");
//
//        filmStorage.updateFilm(updatedFilm);
//
//        Film result = filmStorage.getFilm(filmId);
//        assertEquals("New Name", result.getName());
//    }
//
//    @DisplayName("Тест: Обновить фильм. Фильм не найден")
//    @Test
//    void testUpdateFilm_FilmNotFound() {
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            Film nonExistentFilm = new Film();
//            nonExistentFilm.setId(2);
//            filmStorage.updateFilm(nonExistentFilm);
//        });
//
//        assertEquals("Данный фильм не найден", exception.getMessage());
//    }
//
//    @DisplayName("Тест: Получить фильм. Успешно")
//    @Test
//    void testGetFilm_Success() {
//        filmStorage.addFilm(film);
//
//        int filmId = film.getId();
//
//        Film result = filmStorage.getFilm(filmId);
//
//        assertNotNull(result);
//        assertEquals("Film name", result.getName());
//    }
//
//    @DisplayName("Тест: Получить фильм. Фильм не найден")
//    @Test
//    void testGetFilm_FilmNotFound() {
//        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
//            filmStorage.getFilm(2); // Попытка получить несуществующий фильм
//        });
//
//        assertEquals("Данный фильм не найден", exception.getMessage());
//    }
//
//    @DisplayName("Тест: Получить фильмы")
//    @Test
//    void testGetFilms() {
//        Film film1 = film = new Film(0, "Film name 1", "description 1", LocalDate.of(2010, 12, 31), 100, null);
//        Film film2 = film = new Film(0, "Film name 2", "description 2", LocalDate.of(2020, 12, 31), 100, null);
//        filmStorage.addFilm(film);
//        filmStorage.addFilm(film1);
//        filmStorage.addFilm(film2);
//
//        List<Film> films = filmStorage.getFilms();
//
//        assertEquals(3, films.size());
//    }
//}
