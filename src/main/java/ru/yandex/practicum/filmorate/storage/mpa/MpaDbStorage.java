package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbc;
    private final MpaRowMapper mpaRowMapper;

    private static final String FIND_ALL_SQL =
            "SELECT mpa_id, name FROM mpa_rating ORDER BY mpa_id";

    private static final String FIND_BY_ID_SQL =
            "SELECT mpa_id, name FROM mpa_rating WHERE mpa_id = ?";

    @Override
    public List<MpaRating> getAll() {
        return jdbc.query(FIND_ALL_SQL, mpaRowMapper);
    }

    @Override
    public Optional<MpaRating> getById(int id) {
        try {
            MpaRating mpa = jdbc.queryForObject(FIND_BY_ID_SQL, mpaRowMapper, id);
            return Optional.of(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}