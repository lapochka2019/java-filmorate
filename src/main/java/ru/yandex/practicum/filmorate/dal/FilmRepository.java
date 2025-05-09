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

@Slf4j
@Repository
@RequiredArgsConstructor
public class FilmRepository {

    private final JdbcTemplate jdbcTemplate;
    private final LikesRepository likesRepository;
    private final GenresRepository genresRepository;
    private final MpaRepository mpaRepository;

    public void addFilm(Film film) {
        checkMpaRatingExists(film.getMpa().getId());
        genresRepository.checkGenres(film.getGenres());

        String sql = "INSERT INTO films (name, description, release_date, duration, rate, mpa_rating_id) " +
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

            film.setId(filmId);
            log.info("Выполнено добавление нового фильма в БД. ID фильма: {}", filmId);

            log.info("Вызван метод добавления жанров фильма в БД");
            film.setGenres(genresRepository.setGenresToFilm(filmId, film.getGenres()));

            log.info("Вызван метод добавления лайков фильма в БД");
            film.setLikes(likesRepository.setLikesToFilm(filmId, film.getLikes()));

            updateRate(filmId, film.getLikes().size());

            log.info("Вызван метод добавления Установки МРА");
            film.setMpa(mpaRepository.getMpa(film.getMpa().getId()));

            log.info("Вызван метод Установки жанров");
            film.setGenres(genresRepository.getGenresByFilm(filmId));

            log.info("Добавляем фильм ID {}", film.getId());
            log.info(film.toString());

        } catch (DataIntegrityViolationException ex) {
            log.error("Во время добавления фильма в БД произошла непредвиденная ошибка");
            throw new DataIntegrityViolationException("Не удалось добавить фильм в базу данных.");
        }
    }

    public void updateFilm(Film film) {
        log.info("Обновляем фильм ID {}", film.getId());
        log.info(film.toString());
        checkFilmExists(film.getId());
        //Проверяем возрастной рейтинг
        checkMpaRatingExists(film.getMpa().getId());
        genresRepository.checkGenres(film.getGenres());

        String sql = "UPDATE films SET name=?, description=?, release_date=?, duration=?, rate=?, mpa_rating_id=? " +
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
        log.info("Получаем данные о фильме ID {}", filmId);
        try {
            String sql = "SELECT f.id, " +
                    "       f.name, " +
                    "       f.description, " +
                    "       f.release_date, " +
                    "       f.duration, " +
                    "       f.rate, " +
                    "       m.id AS mpa_id, " +
                    "       m.name AS mpa_name, " +
                    "       LISTAGG(DISTINCT COALESCE(fl.user_id), ',') WITHIN GROUP (ORDER BY fl.user_id) AS likes, " +
                    "       LISTAGG(DISTINCT CONCAT(COALESCE(g.id), ':', COALESCE(g.name, '')), ',') WITHIN GROUP (ORDER BY g.id) AS genres " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                    "LEFT JOIN genre g ON fg.genre_id = g.id " +
                    "WHERE f.id = ? " +
                    "GROUP BY f.id";
            return jdbcTemplate.queryForObject(sql, new FilmDtoMapper(), filmId);
        } catch (EmptyResultDataAccessException ex) {
            log.error("Фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм с ID: " + filmId + " не существует. " + ex);
        } catch (DataAccessException ex) {
            log.error("Ошибка при выполнении запроса: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить фильм. " + ex);
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
                    "       m.id AS mpa_id, " +
                    "       m.name AS mpa_name, " +
                    "       LISTAGG(DISTINCT COALESCE(fl.user_id), ',') WITHIN GROUP (ORDER BY fl.user_id) AS likes, " +
                    "       LISTAGG(DISTINCT CONCAT(COALESCE(g.id), ':', COALESCE(g.name, '')), ',') WITHIN GROUP (ORDER BY g.id) AS genres " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                    "LEFT JOIN genre g ON fg.genre_id = g.id " +
                    "GROUP BY f.id";
            return jdbcTemplate.query(sql, new FilmDtoMapper());
        } catch (EmptyResultDataAccessException ex) {
            log.error("Нет фильмов в БД");
            throw new NotFoundException("Фильмы в БД не найдены. " + ex);
        } catch (DataAccessException ex) {
            log.error("Во время получения фильмов произошла непредвиденная ошибка: {}", ex.getMessage());
            throw new DataIntegrityViolationException("Не удалось получить фильмы");
        }
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
                    "       m.id AS mpa_id, " +
                    "       m.name AS mpa_name, " +
                    "       LISTAGG(DISTINCT COALESCE(fl.user_id), ',') WITHIN GROUP (ORDER BY fl.user_id) AS likes, " +
                    "       LISTAGG(DISTINCT CONCAT(COALESCE(g.id), ':', COALESCE(g.name, '')), ',') WITHIN GROUP (ORDER BY g.id) AS genres " +
                    "FROM films f " +
                    "LEFT JOIN mpa_rating m ON f.mpa_rating_id = m.id " +
                    "LEFT JOIN film_likes fl ON f.id = fl.film_id " +
                    "LEFT JOIN film_genres fg ON f.id = fg.film_id " +
                    "LEFT JOIN genre g ON fg.genre_id = g.id " +
                    "GROUP BY f.id " +
                    "ORDER BY f.rate DESC " +
                    "LIMIT ?";

            return jdbcTemplate.query(sql, new FilmDtoMapper(), limit);
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

    public void checkFilmExists(int filmId) {
        String sql = "SELECT COUNT(*) FROM films WHERE id = ?";
        Boolean isExists = jdbcTemplate.queryForObject(sql, Boolean.class, filmId);

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, filmId);

        if (count == null || count == 0) {
            log.warn("Фильм с ID {} не найден в базе данных", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не существует");
        }

        log.info("Фильм с ID {} найден в базе данных", filmId);
    }

    private void checkMpaRatingExists(int ratingId) {
        String sql = "SELECT COUNT(*) FROM mpa_rating WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, ratingId);
        if (count == null || count == 0) {
            log.error("Пользователь с ID {} не найден", ratingId);
            throw new NotFoundException("Пользователь с ID " + ratingId + " не найден");
        }
    }

    public void updateRate(int filmId, int rate) {
        String updateRateSql = "UPDATE films SET rate = ? WHERE id = ?";
        try {
            jdbcTemplate.update(updateRateSql, rate, filmId);
            log.info("Значение поля rate актуализировано");
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось актуализировать значение поля rate ");
        }
    }

    public void increaseRate(int filmId) {
        // Увеличиваем количество лайков
        String updateRateSql = "UPDATE films SET rate = rate + 1 WHERE id = ?";
        try {
            jdbcTemplate.update(updateRateSql, filmId);
            log.info("Значение поля rate актуализировано");
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось актуализировать значение поля rate ");
        }
    }

    public void decreaseRate(int filmId) {
        // Уменьшаем количество лайков
        String updateRateSql = "UPDATE films SET rate = GREATEST(rate - 1, 0) WHERE id = ?";
        try {
            jdbcTemplate.update(updateRateSql, filmId);
            log.info("Значение поля rate актуализировано");
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось актуализировать значение поля rate ");
        }
    }
}
