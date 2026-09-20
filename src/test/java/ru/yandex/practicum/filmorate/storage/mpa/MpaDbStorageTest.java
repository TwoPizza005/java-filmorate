package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, MpaRowMapper.class})
class MpaDbStorageTest {

    private final MpaDbStorage mpaStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM film");
        jdbc.update("DELETE FROM mpa_rating");
        jdbc.update("INSERT INTO mpa_rating (mpa_id, name) VALUES (1, 'G')");
        jdbc.update("INSERT INTO mpa_rating (mpa_id, name) VALUES (2, 'PG')");
        jdbc.update("INSERT INTO mpa_rating (mpa_id, name) VALUES (3, 'PG-13')");
        jdbc.update("INSERT INTO mpa_rating (mpa_id, name) VALUES (4, 'R')");
        jdbc.update("INSERT INTO mpa_rating (mpa_id, name) VALUES (5, 'NC-17')");
    }

    @Test
    void testGetAll() {
        List<MpaRating> all = mpaStorage.getAll();
        assertThat(all).hasSize(5);
        assertThat(all).extracting(MpaRating::getName)
                .containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void testGetById() {
        Optional<MpaRating> found = mpaStorage.getById(4);
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(m -> {
                    assertThat(m.getId()).isEqualTo(4);
                    assertThat(m.getName()).isEqualTo("R");
                });
    }

    @Test
    void testGetByIdNotFound() {
        assertThat(mpaStorage.getById(999)).isEmpty();
    }
}