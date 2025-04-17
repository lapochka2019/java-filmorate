package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    FilmRepository filmRepository;

    @Autowired
    public FilmService(FilmRepository filmRepository) {
        this.filmRepository = filmRepository;
    }

    //поставить лайк
    public void addLike(int userId, int filmId) {
        filmRepository.addLike(userId, filmId);//выбросит исключение
    }

    //удалить лайк
    public void deleteLike(int userId, int filmId) {
        filmRepository.deleteLike(userId, filmId);
    }

    //топ 10 фильмов (по кол-ву лайков)
    public List<FilmDto> getTopFilms(int limit) {
        return filmRepository.getTopFilms(limit);
    }

    public void addFilm(Film film) {
        filmRepository.addFilm(film);
    }

    public void updateFilm(Film film) {
        filmRepository.updateFilm(film);
    }

    public FilmDto getFilm(int id) {
        return filmRepository.getFilm(id);
    }

    public List<FilmDto> getFilms() {
        return filmRepository.getFilms();
    }
}
