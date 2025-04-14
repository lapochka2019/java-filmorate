package ru.yandex.practicum.filmorate.controller.Film;

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
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest // Загружает полный контекст приложения
@AutoConfigureMockMvc // Настройка MockMvc для тестирования контроллеров
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD) // Очищает контекст после каждого теста
@Sql(scripts = "/schema.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD) // Создание таблиц
@Sql(scripts = "/data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)   // Загрузка данных
@DisplayName("Тесты для проверки валидации Фильма")
public class FilmControllerValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @DisplayName("Создать фильм. Успешно")
    @Test
    public void testCreateFilm_Success() throws Exception {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание тестового фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);
        film.setRate(5);
        film.setMpa(new MpaRating(1));
        film.setLikes(new HashSet<>(Arrays.asList(1, 2, 11)));
        film.setGenres(new HashSet<>(Arrays.asList(new Genre(1), new Genre(2), new Genre(3))));

        // Act & Assert: Выполняем POST-запрос и проверяем результат
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk()) // Ожидаем статус 201 CREATED
                .andExpect(jsonPath("$.name").value("Тестовый фильм"))
                .andExpect(jsonPath("$.description").value("Описание тестового фильма"))
                .andExpect(jsonPath("$.duration").value(120))
                .andExpect(jsonPath("$.likes", containsInAnyOrder(1, 2)));
    }

    @DisplayName("Создать фильм. Некорректное имя")
    @Test
    public void testCreateFilm_InvalidName() throws Exception {
        Film film = new Film();
        film.setName(""); // Некорректное имя (пустое)
        film.setDescription("Описание тестового фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);
        film.setRate(5);
        film.setMpa(new MpaRating(1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("name: Имя не может быть пустым"));
    }

    @DisplayName("Создать фильм. Длинное описание")
    @Test
    public void testCreateFilm_DescriptionTooLong() throws Exception {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("a".repeat(201)); // Превышение лимита в 200 символов
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);
        film.setRate(5);
        film.setMpa(new MpaRating(1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("description: Длина описания не должна превышать 200 символов"));
    }

    @DisplayName("Создать фильм. Ранняя дата выхода")
    @Test
    public void testCreateFilm_ReleaseDateTooEarly() throws Exception {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание тестового фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Дата раньше минимальной
        film.setDuration(120);
        film.setRate(5);
        film.setMpa(new MpaRating(1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("releaseDate: Фильм не может быть снят раньше 28 декабря 1895 года"));
    }

    @DisplayName("Создать фильм. Отрицательный рейтинг")
    @Test
    public void testCreateFilm_NegativeRate() throws Exception {
        Film film = new Film();
        film.setName("Тестовый фильм");
        film.setDescription("Описание тестового фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);
        film.setRate(-5); // Некорректная оценка (отрицательная)
        film.setMpa(new MpaRating(1));

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("rate: Оценка фильма не может быть отрицательной"));
    }

    @DisplayName("Обновить фильм. Успешно")
    @Test
    public void testUpdateFilm_Success() throws Exception {
        Film film = new Film();
        film.setId(1); // ID фильма, который обновляется
        film.setName("Обновленный фильм");
        film.setDescription("Описание обновленного фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(150);
        film.setRate(5);
        film.setMpa(new MpaRating(1));
        film.setLikes(new HashSet<>(Arrays.asList(1, 2)));
        film.setGenres(new HashSet<>(Arrays.asList(new Genre(1), new Genre(2))));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk()) // Ожидаем статус 200 OK
                .andExpect(jsonPath("$.name").value("Обновленный фильм"))
                .andExpect(jsonPath("$.description").value("Описание обновленного фильма"))
                .andExpect(jsonPath("$.duration").value(150))
                .andExpect(jsonPath("$.likes", containsInAnyOrder(1, 2)));
    }

    @DisplayName("Обновить фильм. Некорректное имя")
    @Test
    public void testUpdateFilm_InvalidName() throws Exception {
        Film film = new Film();
        film.setId(1); // ID фильма, который обновляется
        film.setName(""); // Некорректное имя (пустое)
        film.setDescription("Описание обновленного фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(150);
        film.setRate(5);
        film.setMpa(new MpaRating(1));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("name: Имя не может быть пустым"));
    }

    @DisplayName("Обновить фильм. Длинное описание")
    @Test
    public void testUpdateFilm_DescriptionTooLong() throws Exception {
        Film film = new Film();
        film.setId(1); // ID фильма, который обновляется
        film.setName("Обновленный фильм");
        film.setDescription("a".repeat(201)); // Превышение лимита в 200 символов
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(150);
        film.setRate(5);
        film.setMpa(new MpaRating(1));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("description: Длина описания не должна превышать 200 символов"));
    }

    @DisplayName("Обновить фильм. Ранняя дата выхода")
    @Test
    public void testUpdateFilm_ReleaseDateTooEarly() throws Exception {
        Film film = new Film();
        film.setId(1); // ID фильма, который обновляется
        film.setName("Обновленный фильм");
        film.setDescription("Описание обновленного фильма");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // Дата раньше минимальной
        film.setDuration(150);
        film.setRate(5);
        film.setMpa(new MpaRating(1));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("releaseDate: Фильм не может быть снят раньше 28 декабря 1895 года"));
    }

    @DisplayName("Обновить фильм. Отрицательная продолжительность")
    @Test
    public void testUpdateFilm_NegativeDuration() throws Exception {
        Film film = new Film();
        film.setId(1); // ID фильма, который обновляется
        film.setName("Обновленный фильм");
        film.setDescription("Описание обновленного фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(-10); // Некорректная продолжительность (отрицательная)
        film.setRate(5);
        film.setMpa(new MpaRating(1));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("duration: Продолжительность фильма должна быть положительным числом"));
    }

    @DisplayName("Обновить фильм. Отрицательный рейтинг")
    @Test
    public void testUpdateFilm_NegativeRate() throws Exception {
        Film film = new Film();
        film.setId(1); // ID фильма, который обновляется
        film.setName("Обновленный фильм");
        film.setDescription("Описание обновленного фильма");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(150);
        film.setRate(-5); // Некорректная оценка (отрицательная)
        film.setMpa(new MpaRating(1));

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest()) // Ожидаем статус 400 BAD REQUEST
                .andExpect(jsonPath("$.errors[0]").value("rate: Оценка фильма не может быть отрицательной"));
    }
}