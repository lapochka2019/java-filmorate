package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmDtoMapper;
import ru.yandex.practicum.filmorate.model.Film;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

import static java.sql.Types.NULL;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmRepository {

    private final JdbcTemplate jdbcTemplate;
    private final LikesRepository likesRepository;
    private final GenresRepository genresRepository;

    //Создать
    public void addFilm(Film film) {
        //Проверяем возрастной рейтинг
        if (!checkMpaRating(film.getMpa().getId())) {
            film.getMpa().setId(NULL);
            log.error("Возрастное ограничение с id: {} не найдено", film.getMpa());
        }

        String sql = "INSERT INTO film (name, description, release_date, duration, rate, mpa_rating_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        // Создаем KeyHolder для получения сгенерированного ключа
        KeyHolder keyHolder = new GeneratedKeyHolder();
        try {
            // Выполняем запрос с указанием RETURN_GENERATED_KEYS
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, film.getName());
                ps.setString(2, film.getDescription());
                ps.setDate(3, java.sql.Date.valueOf(film.getReleaseDate()));
                ps.setInt(4, film.getDuration());
                ps.setInt(5, film.getRate());
                ps.setInt(6, film.getMpa().getId()); // Предполагается, что getMpa() возвращает ID рейтинга
                return ps;
            }, keyHolder);
            // Получаем последний вставленный id
            int filmId = keyHolder.getKey().intValue();

            log.info("Выполнено добавление нового фильма в БД. ID фильма: {}", filmId);

            // Добавляем жанры
            log.info("Вызван метод добавления жанров фильма в БД");
            film.setGenres(genresRepository.setGenresToFilm(filmId, film.getGenres()));

            // Добавляем лайки
            log.info("Вызван метод добавления лайков фильма в БД");
            film.setLikes(likesRepository.setLikesToFilm(filmId, film.getLikes()));

            // Обновляем поле rate (количество лайков)
            updateRate(filmId, film.getLikes().size());


        } catch (DataIntegrityViolationException ex) {
            log.error("Во время добавления фильма в БД произошла непредвиденная ошибка");
            throw new DataIntegrityViolationException("Не удалось добавить фильм в базу данных.");
        }
    }

    //Обновить
    public void updateFilm(Film film) {
        //Проверяем возрастной рейтинг
        if (!checkMpaRating(film.getMpa().getId())) {
            film.getMpa().setId(NULL);
            log.error("Возрастное ограничение с id: {} не найдено", film.getMpa());
        }

        String sql = "UPDATE film SET name=?, description=?, release_date=?, duration=?, rate=?, mpa_rating_id=? " +
                "WHERE id=?";
        try {
            // Выполняем запрос с указанием RETURN_GENERATED_KEYS
            jdbcTemplate.update(sql,
                    film.getName(),
                    film.getDescription(),
                    java.sql.Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getRate(),
                    film.getMpa().getId(),
                    film.getId());
            log.info("Выполнено обновление фильма: {}", film.getId());

            // Добавляем жанры
            log.info("Вызван метод обновления жанров фильма в БД");
            genresRepository.updateGenres(film.getId(), film.getGenres());

            // Добавляем лайки
            log.info("Вызван метод обновления лайков фильма в БД");
            likesRepository.updateLikes(film.getId(), film.getLikes());

            // Обновляем поле rate (количество лайков)
            updateRate(film.getId(), film.getLikes().size());


        } catch (DataIntegrityViolationException ex) {
            log.error("Во время обновления фильма : {} произошла непредвиденная ошибка", film.getId());
            throw new DataIntegrityViolationException("Не удалось обновить фильм");
        }
    }

    //Получить (1) + жанры + лайки
    public FilmDto getFilm(int filmId) {
        try {
            String sql = "SELECT f.id, " +
                    "       f.name, " +
                    "       f.description, " +
                    "       f.release_date, " +
                    "       f.duration, " +
                    "       f.rate, " +
                    "       m.name AS mpa_name, " +
                    "       LISTAGG(DISTINCT fl.user_id, ',') AS likes, " +
                    "       LISTAGG(DISTINCT g.name, ',') AS genres " +
                    "FROM film f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genre fg ON f.id = fg.film_id " +
                    "LEFT JOIN genre g ON fg.genre_id = g.id " +
                    "WHERE f.id = ? " +
                    "GROUP BY f.id";

            return jdbcTemplate.queryForObject(sql, new Object[]{filmId}, new FilmDtoMapper());
        } catch (EmptyResultDataAccessException ex) {
            log.error("Фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм с ID: " + filmId + " не существует");
        } catch (DataAccessException ex) {
            log.error("Во время получения фильма произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить фильм");
        }
    }

    //Получить (список) + жанры + лайки
    public List<FilmDto> getFilms() {
        try {
            String sql = "SELECT f.id, " +
                    "       f.name, " +
                    "       f.description, " +
                    "       f.release_date, " +
                    "       f.duration, " +
                    "       f.rate, " +
                    "       m.name AS mpa_name, " +
                    "       LISTAGG(DISTINCT fl.user_id, ',') AS likes, " +
                    "       LISTAGG(DISTINCT g.name, ',') AS genres " +
                    "FROM film f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genre fg ON f.id = fg.film_id " +
                    "LEFT JOIN genre g ON fg.genre_id = g.id " +
                    "GROUP BY f.id";
            return jdbcTemplate.query(sql, new FilmDtoMapper());
        } catch (EmptyResultDataAccessException ex) {
            log.error("Нет фильмов в БД");
            throw new NotFoundException("Фильмы в БД не найдены");
        } catch (DataAccessException ex) {
            log.error("Во время получения фильмов произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить фильмы");
        }
    }

    //Поставить лайк
    public void addLike(int userId, int filmId) {
        likesRepository.addLike(filmId, userId);
        increaseRate(filmId);
    }

    //Удалить лайк
    public void deleteLike(int userId, int filmId) {
        likesRepository.removeLike(filmId, userId);
        decreaseRate(filmId);
    }

    //Получить топ (10) фильмов
    public List<FilmDto> getTopFilms(int limit) {
        try {
            String sql = "SELECT f.id, " +
                    "       f.name, " +
                    "       f.description, " +
                    "       f.release_date, " +
                    "       f.duration, " +
                    "       f.rate, " +
                    "       m.name AS mpa_name, " +
                    "       LISTAGG(DISTINCT fl.user_id, ',') AS likes, " +
                    "       LISTAGG(DISTINCT g.name, ',') AS genres " +
                    "FROM film f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genre fg ON f.id = fg.film_id " +
                    "LEFT JOIN genre g ON fg.genre_id = g.id " +
                    "GROUP BY f.id " +
                    "ORDER BY f.rate DESC " + // Сортировка по убыванию количества лайков
                    "LIMIT ?"; // Ограничение на количество фильмов

            return jdbcTemplate.query(sql, new Object[]{limit}, new FilmDtoMapper());
        } catch (DataAccessException ex) {
            log.error("Во время получения фильмов произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить фильмы");
        }
    }

    //Проверяем, есть ли указанный возрастной рейтинг в таблице
    private boolean checkMpaRating(int ratingId) {
        String sql = "SELECT id FROM mpa_rating";
        List<Integer> mpaRatingList = jdbcTemplate.queryForList(sql, Integer.class);
        return mpaRatingList.contains(ratingId);
    }

    public void updateRate(int filmId, int rate) {
        String updateRateSql = "UPDATE film SET rate = ? WHERE id = ?";
        try {
            jdbcTemplate.update(updateRateSql, rate, filmId);
            log.info("Значение поля rate актуализировано");
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось актуализировать значение поля rate ");
        }
    }

    public void increaseRate(int filmId) {
        // Увеличиваем количество лайков
        String updateRateSql = "UPDATE film SET rate = rate + 1 WHERE id = ?";
        try {
            jdbcTemplate.update(updateRateSql, filmId);
            log.info("Значение поля rate актуализировано");
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось актуализировать значение поля rate ");
        }
    }

    public void decreaseRate(int filmId) {
        // Уменьшаем количество лайков
        String updateRateSql = "UPDATE film SET rate = GREATEST(rate - 1, 0) WHERE id = ?";
        try {
            jdbcTemplate.update(updateRateSql, filmId);
            log.info("Значение поля rate актуализировано");
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось актуализировать значение поля rate ");
        }
    }
}
