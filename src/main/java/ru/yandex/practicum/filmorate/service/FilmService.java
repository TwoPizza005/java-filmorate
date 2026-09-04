package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addFilm(Film film) {
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }

    public Collection<Film> getAll() {
        return filmStorage.getAll();
    }

    public void addLike(int filmId, int userId) {
        User user = userStorage.getById(userId);
        Film film = filmStorage.getById(filmId);
        if (film.getLikes().add(userId)) {
            log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
        } else {
            log.warn("Пользователь {} уже лайкал фильм {}", userId, filmId);
        }
    }

    public void removeLike(int filmId, int userId) {
        User user = userStorage.getById(userId);
        Film film = filmStorage.getById(filmId);
        if (film.getLikes().remove(userId)) {
            log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
        } else {
            log.warn("Пользователь {} не лайкал фильм {}", userId, filmId);
        }
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getAll().stream()
                .filter(film -> !film.getLikes().isEmpty())
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(count)
                .collect(Collectors.toList());
    }

    public Film getFilmById(int id) {
        return filmStorage.getById(id);
    }
}