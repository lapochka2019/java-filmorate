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
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FilmController.class) // Аннотация для тестирования веб-контроллера
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Выключение контекста после каждого теста
public class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc; // Инструмент для эмуляции HTTP-запросов

    @Autowired
    private ObjectMapper objectMapper; // Для преобразования объектов в JSON и обратно

    @BeforeEach
    void setUp() throws Exception {
        // Создаем существующего пользователя
        Film film = new Film(1, "Film name1", "Film discription1", LocalDate.of(1999, 02, 03), 100);

        // Имитируем создание пользователя через POST-запрос
        mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk());
    }

    /*Создать фильм*/
    @DisplayName("Тест: Создать фильм. Корректные данные")
    @Test
    void testCreateFilm_Success() throws Exception {
        String jsonFilm = "{\"name\" : \"Film name\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1999-01-01\",\n" +
                "\"duration\" : 150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/films") // URL вашего эндпоинта
                        .contentType(MediaType.APPLICATION_JSON) // Указываем, что передаем JSON
                        .content(jsonFilm)) // Передаем JSON-представление пользователя
                .andExpect(status().isOk()) // Ожидаем статус 200 (OK)
                .andReturn(); // Получаем результат

        String responseContent = result.getResponse().getContentAsString();
        Film createdFilm = objectMapper.readValue(responseContent, Film.class); // Преобразуем ответ обратно в User

        assertEquals(200, result.getResponse().getStatus(), "Статус код должен быть 200 (OK)");
        assertEquals(createdFilm.getName(), "Film name", "Название фильма должно совпадать");
        assertEquals(createdFilm.getDuration(), 150, "Продолжительность фильма должна совпадать");
    }

    @DisplayName("Тест: Создать фильм. Пустое название")
    @Test
    void testCreateFilm_EmptyName() throws Exception {
        String jsonFilm = "{\"name\" : \"\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1999-01-01\",\n" +
                "\"duration\" : 150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
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
        assertEquals("name: Поле \"Имя\" не может быть пустым", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Создать фильм. Длинное описание")
    @Test
    void testCreateFilm_LongName() throws Exception {
        String jsonFilm = "{\"name\" : \"Название фильма\",\n" +
                "\"description\" : " +
                "\"Очень длинное описание фильма, которое должно вызвать ошибку валидации, связанную" +
                "с ограничением по длине поля Описание для фильма, которое очень длинное и превышает две сотни символов." +
                "Очень длинное описание фильма, которое должно вызвать ошибку валидации, связанную " +
                "с ограничением по длине поля Описание для фильма, которое очень длинное и превышает две сотни символов.\",\n" +
                "\"releaseDate\" : \"1999-01-01\",\n" +
                "\"duration\" : 150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
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
        assertEquals("description: Длина описания не должна превышать 200 символов", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Создать фильм. Ранняя дата выхода фильма")
    @Test
    void testCreateFilm_EarlyRelease() throws Exception {
        String jsonFilm = "{\"name\" : \"Film name\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1895-10-10\",\n" +
                "\"duration\" : 150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
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

        assertEquals("releaseDate: Фильм не может быть снять раньше 28 декабря 1895 года", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Создать фильм. Отрицательная продолжительность")
    @Test
    void testCreateFilm_NegativeDuration() throws Exception {
        String jsonFilm = "{\"name\" : \"Film name\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1900-10-10\",\n" +
                "\"duration\" : -150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
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
        assertEquals("duration: Продолжительность фильма должна быть положительным числом", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    /*Обновить фильм*/
    @DisplayName("Тест: Обновить фильм. Корректные данные")
    @Test
    void testUpdateFilm_Success() throws Exception {
        String jsonFilm = "{\"id\": \"1\",\n" +
                "\"name\" : \"Film name\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1999-01-01\",\n" +
                "\"duration\" : 150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/films") // URL вашего эндпоинта
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
                .andExpect(status().isOk()) // Ожидаем статус 200 (OK)
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();
        Film film = objectMapper.readValue(responseContent, Film.class);

        assertEquals(200, result.getResponse().getStatus(), "Статус код должен быть 200 (OK)");
        assertEquals(film.getName(), "Film name", "Название фильма должно совпадать");
        assertEquals(film.getDuration(), 150, "Продолжительность фильма должна совпадать");

    }

    @DisplayName("Тест: Обновить Фильм. Фильм не существует")
    @Test
    void testUpdateUser_Fail() throws Exception {
        String jsonFilm = "{\"id\": \"5\",\n" +
                "\"name\" : \"Name\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1999-01-01\",\n" +
                "\"duration\" : 150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/films") // URL вашего эндпоинта
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
                .andExpect(status().isNotFound())
                .andReturn();

        String responseContent = result.getResponse().getContentAsString();

        assertEquals(404, result.getResponse().getStatus(), "Статус код должен быть 404 (Not Found)");
        assertEquals(responseContent, "Такой фильм не найден!", "Тело ответа не должно быть null");
    }

    @DisplayName("Тест: Обновить фильм. Пустое название")
    @Test
    void testUpdateFilm_EmptyName() throws Exception {
        String jsonFilm = "{\"id\": \"1\",\n" +
                "\"name\" : \"\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1999-01-01\",\n" +
                "\"duration\" : 150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
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
        assertEquals("name: Поле \"Имя\" не может быть пустым", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Обновить фильм. Ранняя дата выхода")
    @Test
    void testUpdateFilm_EarlyRelease() throws Exception {
        String jsonFilm = "{\"id\": \"1\",\n" +
                "\"name\" : \"Film name\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1844-01-01\",\n" +
                "\"duration\" : 150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
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

        assertEquals("releaseDate: Фильм не может быть снять раньше 28 декабря 1895 года", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    @DisplayName("Тест: Обновить фильм. Отрицательная продолжительность")
    @Test
    void testUpdateFilm_NegativeDuration() throws Exception {
        String jsonFilm = "{\"id\": \"1\",\n" +
                "\"name\" : \"Film name\",\n" +
                "\"description\" : \"Film description\",\n" +
                "\"releaseDate\" : \"1999-01-01\",\n" +
                "\"duration\" : -150}";

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonFilm))
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
        assertEquals("duration: Продолжительность фильма должна быть положительным числом", errors.get(0).asText(), "Ошибка должна соответствовать ожидаемому сообщению");
    }

    /*Получить все фильмы*/
    @DisplayName("Тест: Получить список фильмов")
    @Test
    void testGetUsers_Success() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/films")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()) // Ожидаем статус 200 (OK)
                .andReturn();

        // Assert
        String responseContent = result.getResponse().getContentAsString();
        List<Film> film = objectMapper.readValue(responseContent, objectMapper.getTypeFactory().constructCollectionType(List.class, Film.class));

        assertEquals(200, result.getResponse().getStatus(), "Статус код должен быть 200 (OK)");
        assertEquals(1, film.size(), "Должен вернуть один фильм");
    }
}