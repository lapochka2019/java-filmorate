package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class) // Аннотация для тестирования веб-контроллера
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Выключение контекста после каждого теста
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc; // Инструмент для эмуляции HTTP-запросов
    @Autowired
    private ObjectMapper objectMapper; // Для преобразования объектов в JSON и обратно

    @BeforeEach
    void setUp() throws Exception {
        // Создаем существующего пользователя
        User user = new User(1, "correct@mail.ru", "loG_In", "User_Name", LocalDate.of(1999, 02, 03));

        // Имитируем создание пользователя через POST-запрос
        mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

    /*Создать пользователя*/
    @DisplayName("Тест: Создать пользователя. Корректные данные")
    @Test
    void testCreateUser_Success() throws Exception {
        String jsonUser = "{\"email\": \"correct@mail.ru\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users") // URL вашего эндпоинта
                        .contentType(MediaType.APPLICATION_JSON) // Указываем, что передаем JSON
                        .content(jsonUser)) // Передаем JSON-представление пользователя
                .andExpect(status().isOk()) // Ожидаем статус 200 (OK)
                .andReturn(); // Получаем результат

        String responseContent = result.getResponse().getContentAsString();
        User createdUser = objectMapper.readValue(responseContent, User.class); // Преобразуем ответ обратно в User

        assertEquals(200, result.getResponse().getStatus(), "Статус код должен быть 200 (OK)");
        assertEquals("User Name", createdUser.getName(), "Имя пользователя должно совпадать");
        assertEquals("correct@mail.ru", createdUser.getEmail(), "Email пользователя должен совпадать");
    }

    @DisplayName("Тест: Создать пользователя. Пустой email")
    @Test
    void testCreateUser_EmptyEmail() throws Exception {
        String jsonUser = "{\"email\": \"\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("email: Поле \"email\" не может быть пустым", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Создать пользователя. Некорректный email")
    @Test
    void testCreateUser_InvalidEmail() throws Exception {
        String jsonUser = "{\"email\": \"mail\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("email: Это не похоже на email", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Создать пользователя. Логин с пробелами")
    @Test
    void testCreateUser_InvalidLogin() throws Exception {
        String jsonUser = "{\"email\": \"mail@mail.ru\",\n" +
                "    \"login\": \"Log in\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("login: Поле не должно содержать пробелов", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Создать пользователя. Пустой логин")
    @Test
    void testCreateUser_EmptyLogin() throws Exception {
        String jsonUser = "{\"email\": \"mail@mail.ru\",\n" +
                "    \"login\": \"\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("login: Поле \"Логин\" не может быть пустым", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Создать пользователя. Пустое имя")
    @Test
    void testCreateUser_EmptyName() throws Exception {
        String jsonUser = "{\"email\": \"mail@mail.ru\",\n" +
                "    \"login\": \"login123\",\n" +
                "    \"name\": \"\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isOk()) //
                .andReturn();


        String responseContent = result.getResponse().getContentAsString();
        User user = objectMapper.readValue(responseContent, User.class); // Преобразуем ответ обратно в User

        assertEquals(200, result.getResponse().getStatus(), "Статус код должен быть 200 (OK)");
        assertEquals(user.getName(), user.getLogin(), "Тело ответа не должно быть null");
    }

    @DisplayName("Тест: Создать пользователя. Дата рождения в будущем")
    @Test
    void testCreateUser_BirthdayInTheFuture() throws Exception {
        String jsonUser = "{\"email\": \"mail@mail.ru\",\n" +
                "    \"login\": \"Login\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"2026-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("birthday: Дата рождения не может быть в будущем", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    /*Обновить пользователя*/
    @DisplayName("Тест: Обновить пользователя. Корректные данные")
    @Test
    void testUpdateUser_Success() throws Exception {
        String jsonUser = "{ \"id\": \"1\",\n" +
                "    \"email\": \"correct@mail.ru\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users") // URL вашего эндпоинта
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isOk()) // Ожидаем статус 200 (OK)
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        User actualUser = objectMapper.readValue(responseContent, User.class);

        assertEquals(200, result.getResponse().getStatus(), "Статус код должен быть 200 (OK)");
        assertEquals("User Name", actualUser.getName(), "Имя должно быть обновлено");
        assertEquals("correct@mail.ru", actualUser.getEmail(), "Email должен быть обновлен");
        assertEquals("user123", actualUser.getLogin(), "Логин должен быть обновлен");
    }

    @DisplayName("Тест: Обновить пользователя. Пользователь не существует")
    @Test
    void testUpdateUser_Fail() throws Exception {
        String jsonUser = "{ \"id\": \"2\",\n" +
                "    \"email\": \"correct@mail.ru\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users") // URL вашего эндпоинта
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isNotFound())
                .andReturn();

        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(404, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("Такой пользователь не найден!", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Обновить пользователя. Пустой email")
    @Test
    void testUpdateUser_EmptyEmail() throws Exception {
        String jsonUser = "{ \"id\": \"1\",\n" +
                "    \"email\": \"\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("email: Поле \"email\" не может быть пустым", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Обновить пользователя. Некорректный email")
    @Test
    void testUpdateUser_InvalidEmail() throws Exception {
        String jsonUser = "{ \"id\": \"1\",\n" +
                "    \"email\": \"correctmail.ru\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("email: Это не похоже на email", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Обновить пользователя. Пустой логин")
    @Test
    void testUpdateUser_InvalidLogin() throws Exception {
        String jsonUser = "{ \"id\": \"1\",\n" +
                "    \"email\": \"correct@mail.ru\",\n" +
                "    \"login\": \"\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("login: Поле \"Логин\" не может быть пустым", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Обновить пользователя. Логин с пробелами")
    @Test
    void testUpdateUser_EmptyLogin() throws Exception {
        String jsonUser = "{ \"id\": \"1\",\n" +
                "    \"email\": \"correct@mail.ru\",\n" +
                "    \"login\": \"Log In\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("login: Поле не должно содержать пробелов", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Обновить пользователя. Пустое имя")
    @Test
    void testUpdateUser_EmptyName() throws Exception {
        String jsonUser = "{ \"id\": \"1\",\n" +
                "    \"email\": \"correct@mail.ru\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"\",\n" +
                "    \"birthday\": \"1990-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isOk()) //
                .andReturn();


        String responseContent = result.getResponse().getContentAsString();
        User user = objectMapper.readValue(responseContent, User.class); // Преобразуем ответ обратно в User

        assertEquals(200, result.getResponse().getStatus(), "Статус код должен быть 200 (OK)");
        assertEquals(user.getName(), user.getLogin(), "Тело ответа не должно быть null");
    }

    @DisplayName("Тест: Обновить пользователя. Дата рождения в будущем")
    @Test
    void testUpdateUser_BirthdayInTheFuture() throws Exception {
        String jsonUser = "{ \"id\": \"1\",\n" +
                "    \"email\": \"correct@mail.ru\",\n" +
                "    \"login\": \"user123\",\n" +
                "    \"name\": \"User Name\",\n" +
                "    \"birthday\": \"2027-01-01\"}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUser))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 (BAD_REQUEST)
                .andReturn();


        // Получаем тело ответа
        String responseContent = new String(result.getResponse().getContentAsString().getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
        // Преобразуем JSON-ответ в объект Map
        JsonNode responseJson = objectMapper.readTree(responseContent);

        // Проверяем поля ответа
        assertEquals(400, result.getResponse().getStatus(), "Статус код должен быть 400 (BAD_REQUEST)");

        // Проверяем список ошибок
        JsonNode errors = responseJson.get("errors");
        assertEquals("birthday: Дата рождения не может быть в будущем", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    /*Получить список пользователей*/
    @DisplayName("Тест: Получить список пользователей")
    @Test
    void testGetUsers_Success() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Ожидаем статус 200 (OK)
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        List<User> users = objectMapper.readValue(responseContent, objectMapper.getTypeFactory().constructCollectionType(List.class, User.class));

        assertEquals(200, result.getResponse().getStatus(), "Статус код должен быть 200 (OK)");
        assertEquals(1, users.size(), "Должен вернуть одного пользователя");
    }
}