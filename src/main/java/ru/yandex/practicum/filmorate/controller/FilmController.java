package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    FilmService filmService;

    @Autowired
    public FilmController(FilmService service) {
        this.filmService = service;
    }

    //    добавление фильма
    @PostMapping()
    public ResponseEntity<?> createFilm(@Valid @RequestBody Film film) {
        filmService.addFilm(film);
        return ResponseEntity.ok(film);
    }

    //    обновление фильма
    @PutMapping()
    public ResponseEntity<?> updateFilm(@Valid @RequestBody Film film) {
        filmService.updateFilm(film);
        return ResponseEntity.ok(film);
    }

    //получить 1 фильм
    @GetMapping("/{id}")
    public ResponseEntity<?> getFilm(@PathVariable int id) {
        return ResponseEntity.ok(filmService.getFilm(id));
    }

    //    получение всех фильмов
    @GetMapping()
    public ResponseEntity<List<Film>> getFilms() {
        log.info("Запрошен список всех фильмов");
        return ResponseEntity.ok(filmService.getFilms());
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<List<Film>> addLike(@PathVariable("id") int filmId, @PathVariable int userId) {
        log.info("Попытка поставить лайк фильму");
        filmService.addLike(userId, filmId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<List<Film>> deleteLike(@PathVariable("id") int filmId, @PathVariable int userId) {
        log.info("Попытка удалить лайк фильма");
        filmService.deleteLike(userId, filmId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/popular")
    public ResponseEntity<List<Film>> getPopularFilms(@RequestParam(required = false, defaultValue = "10") int count) {
        if (count < 1) {
            throw new NotFoundException("Параметр count не может быть меньше 1");
        }
        log.info("Запрошен список {} популярных фильмов", count);
        return ResponseEntity.ok(filmService.getTopFilms(count));
    }
}
