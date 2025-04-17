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
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Загружает полный контекст приложения
@AutoConfigureMockMvc // Настройка MockMvc для тестирования контроллеров
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Очищает контекст после каждого теста
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // Создание таблиц
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)   // Загрузка данных
@DisplayName("Тесты для проверки валидации Пользователя")
public class UserControllerValidationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Создание пользователя. Успешно")
    void testCreateUser_Success() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user_login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("user@example.com"))
                .andExpect(jsonPath("$.login").value("user_login"))
                .andExpect(jsonPath("$.name").value("User Name"));

    }

    @Test
    @DisplayName("Создание пользователя. Пустой email")
    void testCreateUser_InvalidEmail() throws Exception {
        User user = new User();
        user.setEmail(""); // Некорректный email
        user.setLogin("user_login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("email: Email не может быть пустым"));
    }

    @Test
    @DisplayName("Создание пользователя. Некорректный email")
    void testCreateUser_InvalidEmailFormat() throws Exception {
        User user = new User();
        user.setEmail("invalid-email"); // Некорректный формат email
        user.setLogin("user_login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("email: Это не похоже на email"));
    }

    @Test
    @DisplayName("Создание пользователя. Пустой логин")
    void testCreateUser_InvalidLogin() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(""); // Пустой логин
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors", hasItem("login: Логин не может быть пустым")));
    }

    @Test
    @DisplayName("Создание пользователя. Логин с пробелами")
    void testCreateUser_LoginWithSpaces() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user login"); // Логин с пробелом
        user.setName("User Name");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        // Act & Assert: Выполняем POST-запрос и проверяем результат
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("login: Логин не должен содержать пробелы"));
    }

    @Test
    @DisplayName("Создание пользователя Дата рождения в будущем")
    void testCreateUser_FutureBirthday() throws Exception {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("user_login");
        user.setName("User Name");
        user.setBirthday(LocalDate.of(2030, 1, 1)); // Дата рождения в будущем

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("birthday: Дата рождения не может быть в будущем"));
    }

    @Test
    @DisplayName("Обновление пользователя. Успешно")
    void testUpdateUser_Success() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("updated@example.com");
        user.setLogin("updated_login");
        user.setName("Updated Name");
        user.setBirthday(LocalDate.of(1995, 1, 1));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("updated@example.com"))
                .andExpect(jsonPath("$.login").value("updated_login"))
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @DisplayName("Обновление пользователя. Пользователь не найден")
    void testUpdateUser_NotFound() throws Exception {
        User user = new User();
        user.setId(10);
        user.setEmail("updated@example.com");
        user.setLogin("updated_login");
        user.setName("Updated Name");
        user.setBirthday(LocalDate.of(1995, 1, 1));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Обновление пользователя. Пустой email")
    void testUpdateUser_InvalidEmail() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail(""); // Некорректный email
        user.setLogin("updated_login");
        user.setName("Updated Name");
        user.setBirthday(LocalDate.of(1995, 1, 1));

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("email: Email не может быть пустым"));
    }

    @Test
    @DisplayName("Обновление пользователя. Дата рождения в будущем")
    void testUpdateUser_FutureBirthday() throws Exception {
        User user = new User();
        user.setId(1);
        user.setEmail("updated@example.com");
        user.setLogin("updated_login");
        user.setName("Updated Name");
        user.setBirthday(LocalDate.of(2030, 1, 1)); // Дата рождения в будущем

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("birthday: Дата рождения не может быть в будущем"));
    }
}
