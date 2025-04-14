package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FilmDtoMapper implements RowMapper<FilmDto> {
    @Override
    public FilmDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmDto dto = new FilmDto();

        // Базовые поля
        dto.setId(rs.getInt("id"));
        dto.setName(rs.getString("name"));
        dto.setDescription(rs.getString("description"));
        dto.setReleaseDate(rs.getDate("release_date").toLocalDate());
        dto.setDuration(rs.getInt("duration"));
        dto.setRate(rs.getInt("rate"));

        // Поле MPA
        int mpaId = rs.getInt("mpa_id");
        if (!rs.wasNull()) {
            String mpaName = rs.getString("mpa_name");
            dto.setMpa(new MpaRating(mpaId, mpaName));
        } else {
            dto.setMpa(null); // Устанавливаем null, если MPA отсутствует
        }

// Поле likes
        // Поле likes
        String likesString = rs.getString("likes");
        if (likesString != null && !likesString.isEmpty()) {
            List<Integer> likes = Arrays.stream(likesString.split(","))
                    .map(String::trim)
                    .filter(s -> s != null && !s.trim().isEmpty()) // Фильтруем пустые строки
                    .map(Integer::parseInt)
                    .collect(Collectors.toList()); // Собираем в список
            dto.setLikes(likes);
        } else {
            dto.setLikes(Collections.emptyList()); // Возвращаем пустой список
        }

// Поле genres
        String genresString = rs.getString("genres");
        if (genresString != null && !genresString.isEmpty()) {
            List<Genre> genres = Arrays.stream(genresString.split(","))
                    .map(genre -> {
                        String[] genreParts = genre.trim().split(":");
                        int genreId = Integer.parseInt(genreParts[0].trim());
                        String genreName = genreParts.length > 1 ? genreParts[1].trim() : null;
                        return new Genre(genreId, genreName);
                    })
                    .collect(Collectors.toList()); // Собираем в список
            dto.setGenres(genres);
        } else {
            dto.setGenres(Collections.emptyList()); // Возвращаем пустой список
        }
        return dto;
    }
}
