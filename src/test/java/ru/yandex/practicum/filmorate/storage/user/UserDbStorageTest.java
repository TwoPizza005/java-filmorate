package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTest {

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
    }

    private User makeUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return user;
    }

    @Test
    void testAddUser() {
        User user = userStorage.addUser(makeUser("test@mail.ru", "test"));

        assertThat(user.getId()).isNotNull().isPositive();

        Optional<User> found = userStorage.getById(user.getId());
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(user.getId());
                    assertThat(u.getEmail()).isEqualTo("test@mail.ru");
                    assertThat(u.getLogin()).isEqualTo("test");
                    assertThat(u.getName()).isEqualTo("Имя test");
                    assertThat(u.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
                });
    }

    @Test
    void testUpdateUser() {
        User user = userStorage.addUser(makeUser("old@mail.ru", "old"));

        user.setEmail("new@mail.ru");
        user.setLogin("new");
        user.setName("Новое имя");
        user.setBirthday(LocalDate.of(2000, 5, 5));
        userStorage.updateUser(user);

        Optional<User> found = userStorage.getById(user.getId());
        assertThat(found)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getEmail()).isEqualTo("new@mail.ru");
                    assertThat(u.getLogin()).isEqualTo("new");
                    assertThat(u.getName()).isEqualTo("Новое имя");
                    assertThat(u.getBirthday()).isEqualTo(LocalDate.of(2000, 5, 5));
                });
    }

    @Test
    void testGetByIdNotFound() {
        assertThat(userStorage.getById(999)).isEmpty();
    }

    @Test
    void testGetAll() {
        userStorage.addUser(makeUser("a@mail.ru", "a"));
        userStorage.addUser(makeUser("b@mail.ru", "b"));
        userStorage.addUser(makeUser("c@mail.ru", "c"));

        List<User> all = userStorage.getAll();
        assertThat(all).hasSize(3);
    }

    @Test
    void testGetAllEmpty() {
        assertThat(userStorage.getAll()).isEmpty();
    }

    @Test
    void testDelete() {
        User user = userStorage.addUser(makeUser("del@mail.ru", "del"));
        userStorage.delete(user.getId());

        assertThat(userStorage.getById(user.getId())).isEmpty();
        assertThat(userStorage.getAll()).isEmpty();
    }
}