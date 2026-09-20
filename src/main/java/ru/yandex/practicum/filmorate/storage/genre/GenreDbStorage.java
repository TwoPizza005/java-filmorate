package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbc;
    private final GenreRowMapper genreRowMapper;

    private static final String FIND_ALL_SQL =
            "SELECT genre_id, name FROM genre ORDER BY genre_id";

    private static final String FIND_BY_ID_SQL =
            "SELECT genre_id, name FROM genre WHERE genre_id = ?";

    @Override
    public List<Genre> getAll() {
        return jdbc.query(FIND_ALL_SQL, genreRowMapper);
    }

    @Override
    public Optional<Genre> getById(int id) {
        try {
            Genre genre = jdbc.queryForObject(FIND_BY_ID_SQL, genreRowMapper, id);
            return Optional.of(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}