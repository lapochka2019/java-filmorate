package ru.yandex.practicum.filmorate.dal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mappers.FilmMapper;
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
    private final FilmMapper filmMapper;
    private final LikesRepository likesRepository;
    private final GenresRepository genresRepository;

    //Создать
    public void addFilm(Film film) {
        //Проверяем возрастной рейтинг
        if (!checkMpaRating(film.getMpa())) {
            film.setMpa(NULL);
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
                ps.setInt(6, film.getMpa()); // Предполагается, что getMpa() возвращает ID рейтинга
                return ps;
            }, keyHolder);
            // Получаем последний вставленный id
            int filmId = keyHolder.getKey().intValue();

            log.info("Выполнено добавление нового фильма в БД. ID фильма: {}", filmId);

            // Добавляем жанры
            log.info("Вызван метод добавления жанров фильма в БД");
            genresRepository.setGenresToFilm(filmId, film.getGenres());

            // Добавляем лайки
            log.info("Вызван метод добавления лайков фильма в БД");
            likesRepository.setLikesToFilm(filmId, film.getLikes());

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
        if (!checkMpaRating(film.getMpa())) {
            film.setMpa(NULL);
            log.error("Возрастное ограничение с id: {} не найдено", film.getMpa());
        }

        String sql = "UPDATE film SET name=?, description=?, release_date=?, duration=?, rate=?, mpa_rating_id=?) " +
                "WHERE id=?";

        try {
            // Выполняем запрос с указанием RETURN_GENERATED_KEYS
            jdbcTemplate.update(sql,
                    film.getName(),
                    film.getDescription(),
                    java.sql.Date.valueOf(film.getReleaseDate()),
                    film.getDuration(),
                    film.getMpa(),
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
    public Film getFilm(int filmId) {
        String sql = "SELECT * FROM film WHERE id=?";
        // Используем queryForObject с FilmMapper
        Film film = jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                throw new NotFoundException("Фильм с id: " + filmId + " не найден");
            }
            return filmMapper.mapRow(rs, 0); // Преобразуем ResultSet в Film
        }, filmId);

        if (film == null) {
            throw new NotFoundException("Фильм с id: " + filmId + " не найден");
        }

        // Загружаем лайки
        film.setLikes(likesRepository.getLikesForFilm(filmId));

        // Загружаем жанры
        film.setGenres(genresRepository.getGenresForFilm(filmId));

        return film;
    }

    //Получить (список) + жанры + лайки
    public List<Film> getFilms() {
        String sql = "SELECT * FROM film";

        try {
            // Получаем все фильмы
            List<Film> films = jdbcTemplate.query(sql, filmMapper);

            // Загружаем связанные данные для каждого фильма
            for (Film film : films) {
                int filmId = film.getId();

                // Загружаем лайки
                film.setLikes(likesRepository.getLikesForFilm(filmId));

                // Загружаем жанры
                film.setGenres(genresRepository.getGenresForFilm(filmId));
            }
            return films;
        } catch (DataIntegrityViolationException ex) {
            log.error("Во время получения фильмов произошла непредвиденная ошибка");
            throw new DataIntegrityViolationException("Не удалось получить список фильмов");
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
    public List<Film> getTopFilms(int limit){
        String sql = "SELECT * FROM film ORDER BY rate DESC LIMIT ?";

        try {
            // Получаем все фильмы
            List<Film> films = jdbcTemplate.query(sql, filmMapper, limit);

            // Загружаем связанные данные для каждого фильма
            for (Film film : films) {
                int filmId = film.getId();

                // Загружаем лайки
                film.setLikes(likesRepository.getLikesForFilm(filmId));

                // Загружаем жанры
                film.setGenres(genresRepository.getGenresForFilm(filmId));
            }
            return films;
        } catch (DataIntegrityViolationException ex) {
            log.error("Во время получения фильмов произошла непредвиденная ошибка");
            throw new DataIntegrityViolationException("Не удалось получить список фильмов");
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
        jdbcTemplate.update(updateRateSql, rate, filmId);
        log.info("Значение поля rate актуализировано");
    }

    public void increaseRate(int filmId) {
        // Увеличиваем количество лайков
        String updateRateSql = "UPDATE film SET rate = rate + 1 WHERE id = ?";
        try {
            jdbcTemplate.update(updateRateSql, filmId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось скорректировать рейтинг фильма");
        }
    }

    public void decreaseRate(int filmId){
        // Уменьшаем количество лайков
        String updateRateSql = "UPDATE film SET rate = GREATEST(rate - 1, 0) WHERE id = ?";
        try {
            jdbcTemplate.update(updateRateSql, filmId);
        } catch (DataIntegrityViolationException ex) {
            log.error("Не удалось скорректировать рейтинг фильма");
        }
    }
}
