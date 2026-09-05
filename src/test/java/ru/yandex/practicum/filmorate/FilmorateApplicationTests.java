package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.user.InMemoryUserStorage;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FilmorateApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private InMemoryFilmStorage filmStorage;

    @Autowired
    private InMemoryUserStorage userStorage;

    private Film validFilm;
    private User validUser;

    @BeforeEach
    void setUp() {
        filmStorage.clear();
        userStorage.clear();

        validFilm = new Film();
        validFilm.setName("Матрица");
        validFilm.setDescription("Классика");
        validFilm.setReleaseDate(LocalDate.of(1999, 3, 31));
        validFilm.setDuration(136);

        validUser = new User();
        validUser.setEmail("user@mail.ru");
        validUser.setLogin("user123");
        validUser.setName("Имя");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    private int createUserAndGetId() throws Exception {
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, User.class).getId();
    }

    private int createFilmAndGetId() throws Exception {
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readValue(response, Film.class).getId();
    }

    @Test
    void shouldAddValidFilm() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenFilmNameIsBlank() throws Exception {
        validFilm.setName("");
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название не может быть пустым"));
    }

    @Test
    void shouldFailWhenFilmDescriptionTooLong() throws Exception {
        validFilm.setDescription("a".repeat(201));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Описание не должно превышать 200 символов"));
    }

    @Test
    void shouldAcceptFilmReleaseDateAtMinimum() throws Exception {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 28));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenFilmReleaseDateBeforeMinimum() throws Exception {
        validFilm.setReleaseDate(LocalDate.of(1895, 12, 27));
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.releaseDate").value("Дата релиза должна быть не раньше 28 декабря 1895 года"));
    }

    @Test
    void shouldFailWhenFilmDurationIsZero() throws Exception {
        validFilm.setDuration(0);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.duration").value("Продолжительность должна быть положительным числом"));
    }

    @Test
    void shouldFailWhenFilmDurationIsNegative() throws Exception {
        validFilm.setDuration(-10);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.duration").value("Продолжительность должна быть положительным числом"));
    }

    @Test
    void shouldFailWhenFilmRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailUpdateFilmWhenNameIsBlank() throws Exception {
        int id = createFilmAndGetId();
        validFilm.setId(id);
        validFilm.setName("");
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название не может быть пустым"));
    }

    @Test
    void shouldAddValidUser() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk());
    }

    @Test
    void shouldFailWhenUserEmailIsBlank() throws Exception {
        validUser.setEmail("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email не может быть пустым"));
    }

    @Test
    void shouldFailWhenUserEmailDoesNotContainAt() throws Exception {
        validUser.setEmail("usermail.ru");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Некорректный формат email"));
    }

    @Test
    void shouldFailWhenUserLoginIsBlank() throws Exception {
        validUser.setLogin("");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.login").value("Логин не может быть пустым"));
    }

    @Test
    void shouldFailWhenUserLoginContainsSpaces() throws Exception {
        validUser.setLogin("user 123");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.login").value("Логин не должен содержать пробелы"));
    }

    @Test
    void shouldSetNameToLoginWhenUserNameIsBlank() throws Exception {
        validUser.setName("   ");
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(validUser.getLogin()));
    }

    @Test
    void shouldSetNameToLoginWhenUserNameIsNull() throws Exception {
        validUser.setName(null);
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(validUser.getLogin()));
    }

    @Test
    void shouldFailWhenUserBirthdayInFuture() throws Exception {
        validUser.setBirthday(LocalDate.now().plusDays(1));
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthday").value("Дата рождения не может быть в будущем"));
    }

    @Test
    void shouldFailWhenUserRequestBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFailUpdateUserWhenEmailIsBlank() throws Exception {
        int id = createUserAndGetId();
        validUser.setId(id);
        validUser.setEmail("");
        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Email не может быть пустым"));
    }

    @Test
    void shouldGetUserById() throws Exception {
        int id = createUserAndGetId();
        mockMvc.perform(get("/users/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.email").value(validUser.getEmail()));
    }

    @Test
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id 999 не найден"));
    }

    @Test
    void shouldGetFilmById() throws Exception {
        int id = createFilmAndGetId();
        mockMvc.perform(get("/films/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value(validFilm.getName()));
    }

    @Test
    void shouldReturn404WhenFilmNotFound() throws Exception {
        mockMvc.perform(get("/films/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Фильм с id 999 не найден"));
    }

    @Test
    void shouldAddFriend() throws Exception {
        int userId = createUserAndGetId();
        int friendId = createUserAndGetId();
        mockMvc.perform(put("/users/{userId}/friends/{friendId}", userId, friendId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/{userId}/friends", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(friendId));
    }

    @Test
    void shouldRemoveFriend() throws Exception {
        int userId = createUserAndGetId();
        int friendId = createUserAndGetId();
        mockMvc.perform(put("/users/{userId}/friends/{friendId}", userId, friendId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/users/{userId}/friends/{friendId}", userId, friendId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/users/{userId}/friends", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void shouldReturnCommonFriends() throws Exception {
        int user1 = createUserAndGetId();
        int user2 = createUserAndGetId();
        int commonFriend = createUserAndGetId();
        mockMvc.perform(put("/users/{userId}/friends/{friendId}", user1, commonFriend));
        mockMvc.perform(put("/users/{userId}/friends/{friendId}", user2, commonFriend));
        mockMvc.perform(get("/users/{userId}/friends/common/{otherId}", user1, user2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(commonFriend));
    }

    @Test
    void shouldAddLike() throws Exception {
        int filmId = createFilmAndGetId();
        int userId = createUserAndGetId();
        mockMvc.perform(put("/films/{filmId}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(filmId));
    }

    // ИЗМЕНЕНИЕ ЗДЕСЬ
    @Test
    void shouldRemoveLike() throws Exception {
        int filmId = createFilmAndGetId();
        int userId = createUserAndGetId();
        mockMvc.perform(put("/films/{filmId}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/films/{filmId}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                // Теперь фильм должен остаться в списке, но без лайков
                .andExpect(jsonPath("$[0].id").value(filmId))
                .andExpect(jsonPath("$[0].likes").isEmpty());
    }

    @Test
    void shouldReturnTopFilmsWithCount() throws Exception {
        int film1 = createFilmAndGetId();
        int film2 = createFilmAndGetId();
        int user1 = createUserAndGetId();
        int user2 = createUserAndGetId();
        mockMvc.perform(put("/films/{filmId}/like/{userId}", film1, user1));
        mockMvc.perform(put("/films/{filmId}/like/{userId}", film1, user2));
        mockMvc.perform(put("/films/{filmId}/like/{userId}", film2, user1));
        mockMvc.perform(get("/films/popular?count=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(film1));
        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void shouldReturn404WhenAddingFriendWithNonExistentUser() throws Exception {
        int userId = createUserAndGetId();
        mockMvc.perform(put("/users/{userId}/friends/999", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с id 999 не найден"));
    }

    @Test
    void shouldReturn404WhenLikingNonExistentFilm() throws Exception {
        int userId = createUserAndGetId();
        mockMvc.perform(put("/films/999/like/{userId}", userId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Фильм с id 999 не найден"));
    }
}