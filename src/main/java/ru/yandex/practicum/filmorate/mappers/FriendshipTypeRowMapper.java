package ru.yandex.practicum.filmorate.mappers;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FriendshipType;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FriendshipTypeRowMapper implements RowMapper<FriendshipType> {
    @Override
    public FriendshipType mapRow(ResultSet rs, int rowNum) throws SQLException {
        FriendshipType friendshipType = new FriendshipType();
        friendshipType.setId(rs.getInt("id"));
        friendshipType.setName(rs.getString("name"));
        return friendshipType;
    }
}
