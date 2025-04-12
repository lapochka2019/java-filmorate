package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FilmService {
    FilmStorage filmStorage;
    UserStorage userStorage;
    FilmRepository filmRepository;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage,FilmRepository filmRepository) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.filmRepository = filmRepository;
    }

    //поставить лайк
    public void addLike(int userId, int filmId) {
        log.info("Пользователь {} пытается поставить лайк фильму {}", userId, filmId);
        try {
            userStorage.getUser(userId);//выбросит исключение
            Film film = filmStorage.getFilm(filmId);
            film.addLike(userId);
            log.info("Лайк поставлен успешно");
        } catch (NotFoundException e) {
            log.warn("Поставить лайк не удалось: {}", e.getMessage());
            throw e;
        }
    }

    //удалить лайк
    public void deleteLike(int userId, int filmId) {
        log.info("Пользователь {} пытается удалить лайк у фильма {}", userId, filmId);
        try {
            userStorage.getUser(userId);//выбросит исключение
            Film film = filmStorage.getFilm(filmId);
            film.removeLike(userId);
            log.info("Лайк удален успешно");
        } catch (NotFoundException e) {
            log.warn("Удалить лайк не удалось: {}", e.getMessage());
            throw e;
        }
    }

    //топ 10 фильмов (по кол-ву лайков)
    public List<Film> getTopFilms(int limit) {
        log.info("Получены лучшие {} фильмов", limit);
        return filmStorage.getTopFilms(limit);
    }

    public void addFilm(Film film) {
        filmRepository.addFilm(film);
    }

    public void updateFilm(Film film) {
        filmStorage.updateFilm(film);
    }

    public Film getFilm(int id) {
        return filmStorage.getFilm(id);
    }

    public ArrayList<Film> getFilms() {
        return filmStorage.getFilms();
    }
}
