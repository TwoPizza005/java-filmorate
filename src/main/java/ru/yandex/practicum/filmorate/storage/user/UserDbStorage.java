package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper userRowMapper;

    // ==================== SQL ====================

    private static final String FIND_ALL_SQL =
            "SELECT user_id, email, login, name, birthday FROM users ORDER BY user_id";

    private static final String FIND_BY_ID_SQL =
            "SELECT user_id, email, login, name, birthday FROM users WHERE user_id = ?";

    private static final String ADD_USER_SQL =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_USER_SQL =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";

    private static final String DELETE_USER_SQL =
            "DELETE FROM users WHERE user_id = ?";

    // ==================== Методы ====================

    @Override
    public User addUser(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    ADD_USER_SQL, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        jdbc.update(UPDATE_USER_SQL,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public Optional<User> getById(int id) {
        try {
            User user = jdbc.queryForObject(FIND_BY_ID_SQL, userRowMapper, id);
            return Optional.of(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> getAll() {
        return jdbc.query(FIND_ALL_SQL, userRowMapper);
    }

    @Override
    public void delete(int id) {
        jdbc.update(DELETE_USER_SQL, id);
    }
}