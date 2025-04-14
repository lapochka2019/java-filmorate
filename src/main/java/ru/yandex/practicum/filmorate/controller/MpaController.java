package ru.yandex.practicum.filmorate.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/mpa")
@Validated
@AllArgsConstructor

public class MpaController {

    MpaService mpaService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getGenre(@PathVariable int id) {
        return ResponseEntity.ok(mpaService.getMpa(id));
    }

    @GetMapping
    public ResponseEntity<List<FilmDto>> getFilmWithGenre() {
        return ResponseEntity.ok(mpaService.getFilmsWithMpa());
    }
}
