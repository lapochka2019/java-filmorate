package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.FilmDto;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Component
public class FilmDtoMapper implements RowMapper<FilmDto> {
    @Override
    public FilmDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        FilmDto dto = new FilmDto();

        dto.setId(rs.getInt("id"));
        dto.setName(rs.getString("name"));
        dto.setDescription(rs.getString("description"));
        dto.setReleaseDate(rs.getDate("release_date").toLocalDate());
        dto.setDuration(rs.getInt("duration"));
        dto.setRate(rs.getInt("rate"));
        dto.setMpa(rs.getString("mpa_name"));

        // Обработка NULL для likes
        String likesString = rs.getString("likes");
        if (likesString != null && !likesString.isEmpty()) {
            String[] likesArray = likesString.split(",");
            Set<Integer> likes = new HashSet<>();
            for (String like : likesArray) {
                likes.add(Integer.parseInt(like.trim()));
            }
            dto.setLikes(likes);
        }

        // Обработка NULL для genres
        String genresString = rs.getString("genres");
        if (genresString != null && !genresString.isEmpty()) {
            String[] genresArray = genresString.split(",");
            Set<String> genres = new HashSet<>();
            for (String genre : genresArray) {
                genres.add(genre.trim());
            }
            dto.setGenres(genres);
        }

        return dto;
    }
}
