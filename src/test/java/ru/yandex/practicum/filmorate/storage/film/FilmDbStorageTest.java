package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class,
        UserDbStorage.class, UserRowMapper.class})
class FilmDbStorageTest {

    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM friendship");
        jdbc.update("DELETE FROM film");
        jdbc.update("DELETE FROM users");
        jdbc.update("DELETE FROM genre");
        jdbc.update("DELETE FROM mpa_rating");

        jdbc.update("INSERT INTO mpa_rating (mpa_id, name) VALUES (1, 'G')");
        jdbc.update("INSERT INTO mpa_rating (mpa_id, name) VALUES (2, 'PG')");
        jdbc.update("INSERT INTO genre (genre_id, name) VALUES (1, 'Комедия')");
        jdbc.update("INSERT INTO genre (genre_id, name) VALUES (2, 'Драма')");
    }

    private Film makeFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание " + name);
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        MpaRating mpa = new MpaRating();
        mpa.setId(1);
        mpa.setName("G");
        film.setMpa(mpa);

        return film;
    }

    private User makeUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.addUser(user);
    }

    @Test
    void testAddFilm() {
        Film film = filmStorage.addFilm(makeFilm("Матрица"));

        assertThat(film.getId()).isPositive();

        Optional<Film> found = filmStorage.getById(film.getId());
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(film.getId());
                    assertThat(f.getName()).isEqualTo("Матрица");
                    assertThat(f.getDescription()).isEqualTo("Описание Матрица");
                    assertThat(f.getDuration()).isEqualTo(120);
                    assertThat(f.getMpa().getId()).isEqualTo(1);
                    assertThat(f.getMpa().getName()).isEqualTo("G");
                });
    }

    @Test
    void testAddFilmWithGenres() {
        Film film = makeFilm("Фильм с жанрами");
        film.setGenres(Set.of(new Genre(1, "Комедия"), new Genre(2, "Драма")));

        Film created = filmStorage.addFilm(film);

        Optional<Film> found = filmStorage.getById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres())
                .hasSize(2)
                .extracting(Genre::getName)
                .containsExactlyInAnyOrder("Комедия", "Драма");
    }

    @Test
    void testUpdateFilm() {
        Film film = filmStorage.addFilm(makeFilm("Старое"));

        film.setName("Новое");
        film.setDescription("Новое описание");
        film.setDuration(150);
        filmStorage.updateFilm(film);

        Optional<Film> found = filmStorage.getById(film.getId());
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getName()).isEqualTo("Новое");
                    assertThat(f.getDescription()).isEqualTo("Новое описание");
                    assertThat(f.getDuration()).isEqualTo(150);
                });
    }

    @Test
    void testUpdateFilmChangesGenres() {
        Film film = makeFilm("Фильм");
        film.setGenres(Set.of(new Genre(1, "Комедия")));
        Film created = filmStorage.addFilm(film);

        created.setGenres(Set.of(new Genre(2, "Драма")));
        filmStorage.updateFilm(created);

        Optional<Film> found = filmStorage.getById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getGenres())
                .hasSize(1)
                .extracting(Genre::getName)
                .containsExactly("Драма");
    }

    @Test
    void testGetByIdNotFound() {
        assertThat(filmStorage.getById(999)).isEmpty();
    }

    @Test
    void testGetAll() {
        filmStorage.addFilm(makeFilm("A"));
        filmStorage.addFilm(makeFilm("B"));
        filmStorage.addFilm(makeFilm("C"));

        List<Film> all = filmStorage.getAll();
        assertThat(all).hasSize(3);
    }

    @Test
    void testDelete() {
        Film film = filmStorage.addFilm(makeFilm("Удаляемый"));
        filmStorage.delete(film.getId());

        assertThat(filmStorage.getById(film.getId())).isEmpty();
    }

    @Test
    void testAddLike() {
        Film film = filmStorage.addFilm(makeFilm("Фильм"));
        User user = makeUser("like@mail.ru", "like");

        filmStorage.addLike(film.getId(), user.getId());

        Optional<Film> found = filmStorage.getById(film.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).containsExactly(user.getId());
    }

    @Test
    void testRemoveLike() {
        Film film = filmStorage.addFilm(makeFilm("Фильм"));
        User user = makeUser("like@mail.ru", "like");

        filmStorage.addLike(film.getId(), user.getId());
        filmStorage.removeLike(film.getId(), user.getId());

        Optional<Film> found = filmStorage.getById(film.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLikes()).isEmpty();
    }

    @Test
    void testGetPopular() {
        Film film1 = filmStorage.addFilm(makeFilm("Популярный"));
        Film film2 = filmStorage.addFilm(makeFilm("Менее популярный"));
        User user1 = makeUser("u1@mail.ru", "u1");
        User user2 = makeUser("u2@mail.ru", "u2");

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        List<Film> popular = filmStorage.getPopular(10);

        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
        assertThat(popular.get(1).getId()).isEqualTo(film2.getId());
    }

    @Test
    void testGetPopularWithLimit() {
        Film film1 = filmStorage.addFilm(makeFilm("Популярный"));
        Film film2 = filmStorage.addFilm(makeFilm("Менее популярный"));
        User user1 = makeUser("u1@mail.ru", "u1");
        User user2 = makeUser("u2@mail.ru", "u2");

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        List<Film> popular = filmStorage.getPopular(1);

        assertThat(popular).hasSize(1);
        assertThat(popular.get(0).getId()).isEqualTo(film1.getId());
    }

    @Test
    void testGetPopularEmpty() {
        assertThat(filmStorage.getPopular(10)).isEmpty();
    }
}