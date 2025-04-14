package ru.yandex.practicum.filmorate.controller.User;

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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Загружает полный контекст приложения
@AutoConfigureMockMvc // Настройка MockMvc для тестирования контроллеров
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Очищает контекст после каждого теста
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // Создание таблиц
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)   // Загрузка данных
@DisplayName("Тесты для проверки UserController")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Получение пользователя. Успешно")
    void testGetUser_Success() throws Exception {
        mockMvc.perform(get("/users/{id}", 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("user1@example.com"))
                .andExpect(jsonPath("$.login").value("user1_login"));
    }

    @Test
    @DisplayName("Получение пользователя. Пользователь не найден")
    void testGetUser_NotFound() throws Exception {
        mockMvc.perform(get("/users/{id}", 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с ID 999 не найден"));
    }

    @Test
    @DisplayName("Получение всех пользователей. Успешно")
    void testGetUsers_Success() throws Exception {
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].email").value("user2@example.com"));
    }

    @Test
    @Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @DisplayName("Получение всех пользователей. База данных пуста)")
    void testGetUsers_EmptyDatabase() throws Exception {
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Добавление в друзья. Успешно")
    void testAddFriend_Success() throws Exception {
        mockMvc.perform(put("/users/{id}/friends/{friendId}", 1, 4)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Добавление в друзья. Пользователь не найден)")
    void testAddFriend_UserNotFound() throws Exception {
        mockMvc.perform(put("/users/{id}/friends/{friendId}", 999, 2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Подтвердить дружбу. Успешно")
    void testConfirmFriend_Success() throws Exception {
        mockMvc.perform(put("/users/{id}/friends/{friendId}", 3, 1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление из друзей. Успешно")
    void testDeleteFriend_Success() throws Exception {
        mockMvc.perform(delete("/users/{id}/friends/{friendId}", 1, 3)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление из друзей. Отменить одобрение дружбы. Успешно")
    void testDeleteConfirmFriend_Success() throws Exception {
        mockMvc.perform(delete("/users/{id}/friends/{friendId}", 1, 2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Удаление из друзей - неуспешный исход (друг не найден)")
    void testDeleteFriend_FriendNotFound() throws Exception {
        mockMvc.perform(delete("/users/{id}/friends/{friendId}", 1, 999)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()) // Ожидаем статус 404 NOT FOUND
                .andExpect(jsonPath("$.message").value("Данные пользователи не являются друзьями"));
    }

    @Test
    @DisplayName("Получение всех друзей. Успешно")
    void testGetFriends_Success() throws Exception {
        mockMvc.perform(get("/users/{id}/friends", 2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user1@example.com"))
                .andExpect(jsonPath("$[1].email").value("user4@example.com"));

    }

    @Test
    @DisplayName("Получение всех друзей. У пользователя нет друзей)")
    void testGetFriends_NoFriends() throws Exception {
        mockMvc.perform(get("/users/{id}/friends", 5)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Получение общих друзей. Успешно")
    void testGetMutualFriends_Success() throws Exception {
        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", 1, 4)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("user2@example.com"));
    }

    @Test
    @DisplayName("Получение общих друзей. Нет общих друзей)")
    void testGetMutualFriends_NoMutualFriends() throws Exception {
        mockMvc.perform(get("/users/{id}/friends/common/{otherId}", 1, 2)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
