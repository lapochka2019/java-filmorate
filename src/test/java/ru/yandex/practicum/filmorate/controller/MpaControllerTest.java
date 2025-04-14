package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Загружает полный контекст приложения
@AutoConfigureMockMvc // Настройка MockMvc для тестирования контроллеров
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Очищает контекст после каждого теста
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // Создание таблиц
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)   // Загрузка данных
@DisplayName("Тесты для проверки MpaController")
public class MpaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Получение MPA по ID. Успешно")
    void testGetMpaById_Success() throws Exception {
        mockMvc.perform(get("/mpa/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("G"));
    }

    @Test
    @DisplayName("Получение MPA по ID. MPA не найден")
    void testGetMpaById_NotFound() throws Exception {
        mockMvc.perform(get("/mpa/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Ожидаем статус 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("MPA с ID 999 не найден"));
    }

    @Test
    @DisplayName("Получение фильмов с MPA. Успешно")
    void testGetFilmsWithMpa_Success() throws Exception {
        mockMvc.perform(get("/mpa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Фильм 1"))
                .andExpect(jsonPath("$[1].name").value("Фильм 2"));

    }

    @Test
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Получение фильмов с MPA . База данных пуста")
    void testGetFilmsWithMpa_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/mpa")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
