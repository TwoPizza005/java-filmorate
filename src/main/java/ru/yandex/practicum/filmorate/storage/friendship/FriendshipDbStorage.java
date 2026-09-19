package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FriendshipDbStorage implements FriendshipStorage {

    private final JdbcTemplate jdbc;

    private final UserRowMapper userRowMapper;

    private static final String ADD_FRIEND_SQL =
            "INSERT INTO friendship (user_id, friend_id, friend_status) VALUES (?, ?, 'UNCONFIRMED')";

    private static final String CONFIRM_FRIEND_SQL =
            "UPDATE friendship SET friend_status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?";

    private static final String REMOVE_FRIEND_SQL =
            "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";

    private static final String COUNT_SQL =
            "SELECT COUNT(*) FROM friendship WHERE user_id = ? AND friend_id = ?";

    private static final String GET_FRIENDS_SQL =
            "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
                    "FROM users u " +
                    "JOIN friendship f ON u.user_id = f.friend_id " +
                    "WHERE f.user_id = ? " +
                    "ORDER BY u.user_id";

    private static final String GET_COMMON_FRIENDS_SQL =
            "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
                    "FROM users u " +
                    "JOIN friendship f1 ON u.user_id = f1.friend_id " +
                    "JOIN friendship f2 ON u.user_id = f2.friend_id " +
                    "WHERE f1.user_id = ? AND f2.user_id = ?";

    @Override
    public void addFriend(int userId, int friendId) {
        Integer existing = jdbc.queryForObject(COUNT_SQL, Integer.class, userId, friendId);
        if (existing != null && existing > 0) {
            return;
        }

        Integer incoming = jdbc.queryForObject(COUNT_SQL, Integer.class, friendId, userId);

        if (incoming != null && incoming > 0) {
            jdbc.update(CONFIRM_FRIEND_SQL, friendId, userId);
            jdbc.update("INSERT INTO friendship (user_id, friend_id, friend_status) " +
                    "VALUES (?, ?, 'CONFIRMED')", userId, friendId);
        } else {
            jdbc.update(ADD_FRIEND_SQL, userId, friendId);
        }
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        jdbc.update(REMOVE_FRIEND_SQL, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        return jdbc.query(GET_FRIENDS_SQL, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        return jdbc.query(GET_COMMON_FRIENDS_SQL, userRowMapper, userId, otherId);
    }
}