package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    protected Map<Integer, Film> films;
    protected int idCounter = 0;

    public InMemoryFilmStorage() {
        films = new HashMap<>();
    }

    @Override
    public int generateId() {
        return ++idCounter;
    }

    @Override
    public void addFilm(Film film) {
        film.setId(generateId());
        putFilmLikes(film);
        films.put(idCounter, film);
        log.info("Успешно создан новый фильм: {}", film);
    }

    @Override
    public void deleteFilm(int id) {
        log.info("Попытка удалить фильм: {}", id);
        if (films.containsKey(id)) {
            films.remove(id);
            log.info("Фильм успешно удален");

        } else {
            log.warn("Не удалось удалить фильм");
            throw new NotFoundException("Данный фильм не найден");
        }
    }

    @Override
    public void updateFilm(Film film) {
        log.info("Попытка обновить фильм: {}", film);
        if (films.containsKey(film.getId())) {
            putFilmLikes(film);
            films.put(film.getId(), film);
            log.info("Фильм успешно обновлен");
        } else {
            log.warn("Не удалось обновить фильм");
            throw new NotFoundException("Данный фильм не найден");
        }
    }

    @Override
    public Film getFilm(int id) {
        log.info("Попытка получить данные о фильме: {}", id);
        if (films.containsKey(id)) {
            log.info("Данные о фильме успешно получены");
            return films.get(id);
        } else {
            log.warn("Не удалось получить данные");
            throw new NotFoundException("Данный фильм не найден");
        }
    }

    @Override
    public ArrayList<Film> getFilms() {
        log.info("Успешно получен список всех фильмов");
        return new ArrayList<>(films.values());
    }

    public void putFilmLikes(Film film) {
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
            log.info("likes Set фильма заменен на пустой HashSet");
        }
    }

    @Override
    public List<Film> getTopFilms(int limit) {
        return films.values().stream()
                .sorted((film1, film2) -> Integer.compare(film2.getLikes().size(), film1.getLikes().size())) // Сортируем по убыванию количества лайков
                .limit(limit) // Берем первые 10 фильмов
                .collect(Collectors.toList()); // Преобразуем в список
    }
}
