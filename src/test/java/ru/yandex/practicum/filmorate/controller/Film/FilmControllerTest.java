package ru.yandex.practicum.filmorate.controller.Film;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Загружает полный контекст приложения
@AutoConfigureMockMvc // Настройка MockMvc для тестирования контроллеров
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Очищает контекст после каждого теста
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // Создание таблиц
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)   // Загрузка данных
@DisplayName("Тесты для проверки FilmController")
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("Получение одного фильма по ID. Успешно")
    void testGetFilmById_Success() throws Exception {
        mockMvc.perform(get("/films/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Фильм 1"));
    }

    @Test
    @DisplayName("Получение одного фильма по ID. Не успешно")
    void testGetFilmById_NotFound() throws Exception {
        mockMvc.perform(get("/films/{id}", 10)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение всех фильмов. Успешно")
    void testGetAllFilms_Success() throws Exception {
        mockMvc.perform(get("/films")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Фильм 1"))
                .andExpect(jsonPath("$[1].name").value("Фильм 2"));
    }

    @Test
    @DisplayName("Получение всех фильмов. База данных пуста")
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testGetAllFilms_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/films")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Добавить лайк. Успешно")
    void testAddLike_Success() throws Exception {
        //Выполняем PUT-запрос и проверяем результат
        mockMvc.perform(put("/films/{id}/like/{userId}", 1, 5)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Добавить лайк. Фильм не найден")
    void testAddLike_FilmNotFound() throws Exception {
        // Выполняем PUT-запрос и проверяем результат
        mockMvc.perform(put("/films/{id}/like/{userId}", 999, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Ожидаем статус 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("Фильм с id 999 не найден"));
    }

    @Test
    @DisplayName("Добавить лайк. Пользователь не найден")
    void testAddLike_UserNotFound() throws Exception {
        // Выполняем PUT-запрос и проверяем результат
        mockMvc.perform(put("/films/{id}/like/{userId}", 1, 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Ожидаем статус 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("Пользователь с id 999 не найден"));
    }

    @Test
    @DisplayName("Удалить лайк. Успешно")
    void testDeleteLike_Success() throws Exception {
        //Выполняем PUT-запрос и проверяем результат
        mockMvc.perform(delete("/films/{id}/like/{userId}", 1, 3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удалить лайк. Фильм не найден")
    void testDeleteLike_FilmNotFound() throws Exception {
        // Выполняем PUT-запрос и проверяем результат
        mockMvc.perform(delete("/films/{id}/like/{userId}", 999, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удалить лайк. Пользователь не найден")
    void testDeleteLike_UserNotFound() throws Exception {
        // Выполняем PUT-запрос и проверяем результат
        mockMvc.perform(delete("/films/{id}/like/{userId}", 1, 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Получение популярных фильмов. Успешно")
    void testGetPopularFilms_Success() throws Exception {
        mockMvc.perform(get("/films/popular")
                        .param("count", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Фильм 1"))
                .andExpect(jsonPath("$[1].name").value("Фильм 2"));
    }

    @Test
    @DisplayName("Получение популярных фильмов. Некорректный параметр count)")
    void testGetPopularFilms_InvalidCount() throws Exception {
        mockMvc.perform(get("/films/popular")
                        .param("count", "-1") // Невалидное значение
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Ожидаем статус 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("Параметр count не может быть меньше 1"));
    }

}
