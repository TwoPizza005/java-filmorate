package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTest {

    private final GenreDbStorage genreStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM genre");
        jdbc.update("INSERT INTO genre (genre_id, name) VALUES (1, 'Комедия')");
        jdbc.update("INSERT INTO genre (genre_id, name) VALUES (2, 'Драма')");
        jdbc.update("INSERT INTO genre (genre_id, name) VALUES (3, 'Мультфильм')");
    }

    @Test
    void testGetAll() {
        List<Genre> all = genreStorage.getAll();
        assertThat(all).hasSize(3);
        assertThat(all).extracting(Genre::getName)
                .containsExactly("Комедия", "Драма", "Мультфильм");
    }

    @Test
    void testGetById() {
        Optional<Genre> found = genreStorage.getById(2);
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(g -> {
                    assertThat(g.getId()).isEqualTo(2);
                    assertThat(g.getName()).isEqualTo("Драма");
                });
    }

    @Test
    void testGetByIdNotFound() {
        assertThat(genreStorage.getById(999)).isEmpty();
    }
}
