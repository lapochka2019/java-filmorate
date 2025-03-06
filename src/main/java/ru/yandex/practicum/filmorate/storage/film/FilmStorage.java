package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.Map;

public interface FilmStorage {
    Map<Integer, Film> films = null;

    int generateId();

    void addFilm(Film film);

    void deleteFilm(int id);

    void updateFilm(Film film);

    Film getFilm(int id);

    ArrayList<Film> getFilms();
}
