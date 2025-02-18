package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private int id = 1;

    //    добавление фильма
    @PostMapping()
    public ResponseEntity<?> createFilm(@Valid @RequestBody Film film) {
        //Не стала делать проверку на пустую сущность
        //так как предусмотрена валидация и хоть какие-то данные должны быть
        addId(film);
        log.info("Успешно создан новый фильм: {}", film);
        films.put(film.getId(), film);
        return ResponseEntity.ok(film);
    }

    //    обновление фильма
    @PutMapping()
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) {
        //Не стала делать проверку на пустую сущность
        //так как предусмотрена валидация и хоть какие-то данные должны быть
        //проверяем, что такой пользователь уже есть в "БД" и
        if (!films.containsKey(film.getId())) {
            log.warn("Попытка изменить фильм, который еще не создан");
            return new ResponseEntity<>("Такой фильм не найден!", HttpStatus.NOT_FOUND);
        }
        log.info("Успешно обновлен фильм: {}", film);
        films.put(film.getId(), film);
        return ResponseEntity.ok(film);
    }

    //    получение всех фильмов
    @GetMapping()
    public ResponseEntity<List<Film>> getFilms() {
        log.info("Запрошен список всех пользователей");
        return ResponseEntity.ok(new ArrayList<>(films.values()));
    }

    private void addId(Film film) {
        film.setId(id);
        id++;
    }
}
