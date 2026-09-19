package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper filmRowMapper;
    private final GenreRowMapper genreRowMapper;

    private static final String FIND_ALL_SQL =
            "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                    "f.mpa_id, m.name AS mpa_name " +
                    "FROM film f " +
                    "JOIN mpa_rating m ON f.mpa_id = m.mpa_id " +
                    "ORDER BY f.film_id";

    private static final String FIND_BY_ID_SQL =
            "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                    "f.mpa_id, m.name AS mpa_name " +
                    "FROM film f " +
                    "JOIN mpa_rating m ON f.mpa_id = m.mpa_id " +
                    "WHERE f.film_id = ?";

    private static final String ADD_FILM =
            "INSERT INTO film (film_name, description, release_date, duration, mpa_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_FILM =
            "UPDATE film SET film_name = ?, description = ?, release_date = ?, " +
                    "duration = ?, mpa_id = ? WHERE film_id = ?";

    private static final String DELETE_FILM =
            "DELETE FROM film WHERE film_id = ?";

    private static final String FIND_GENRES_SQL =
            "SELECT g.genre_id, g.name " +
                    "FROM genre g " +
                    "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id = ? " +
                    "ORDER BY g.genre_id";

    private static final String FIND_LIKES_SQL =
            "SELECT user_id FROM likes WHERE film_id = ?";

    private static final String INSERT_GENRE_SQL =
            "INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)";

    private static final String DELETE_GENRES =
            "DELETE FROM film_genre WHERE film_id = ?";

    private static final String FIND_POPULAR_SQL =
            "SELECT f.film_id, f.film_name, f.description, f.release_date, f.duration, " +
                    "f.mpa_id, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                    "FROM film f " +
                    "JOIN mpa_rating m ON f.mpa_id = m.mpa_id " +
                    "LEFT JOIN likes l ON f.film_id = l.film_id " +
                    "GROUP BY f.film_id, f.film_name, f.description, f.release_date, f.duration, f.mpa_id, m.name " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";

    private static final String ADD_LIKE_SQL =
            "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";

    private static final String REMOVE_LIKE_SQL =
            "DELETE FROM likes WHERE film_id = ? AND user_id = ?";

    @Override
    public Film addFilm(Film film) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    ADD_FILM, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());
        saveGenres(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        jdbc.update(UPDATE_FILM,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        jdbc.update(DELETE_GENRES, film.getId());
        saveGenres(film);

        return film;
    }

    @Override
    public Optional<Film> getById(int id) {
        try {
            Film film = jdbc.queryForObject(FIND_BY_ID_SQL, filmRowMapper, id);

            List<Genre> genres = jdbc.query(FIND_GENRES_SQL, genreRowMapper, id);
            film.setGenres(new LinkedHashSet<>(genres));

            List<Integer> likes = jdbc.queryForList(FIND_LIKES_SQL, Integer.class, id);
            film.setLikes(new HashSet<>(likes));

            return Optional.of(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAll() {
        return jdbc.query(FIND_ALL_SQL, filmRowMapper);
    }

    @Override
    public List<Film> getPopular(int count) {
        return jdbc.query(FIND_POPULAR_SQL, filmRowMapper, count);
    }

    @Override
    public void delete(int id) {
        jdbc.update(DELETE_FILM, id);
    }

    @Override
    public void addLike(int filmId, int userId) {
        jdbc.update(ADD_LIKE_SQL, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        jdbc.update(REMOVE_LIKE_SQL, filmId, userId);
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbc.update(INSERT_GENRE_SQL, film.getId(), genre.getId());
        }
    }
}