package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenresRepository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class GenreService {

    GenresRepository genresRepository;

    public Genre getGenre(int id) {
        return genresRepository.getGenre(id);
    }

    public List<FilmDto> getFilmsWithGenre() {
        return genresRepository.getFilmsWithGenre();
    }
}
