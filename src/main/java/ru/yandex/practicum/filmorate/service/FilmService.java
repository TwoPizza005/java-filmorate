package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;

    private final UserStorage userStorage;

    private final MpaStorage mpaStorage;

    private final GenreStorage genreStorage;

    public Film addFilm(Film film) {
        validateMpaAndGenres(film);
        return filmStorage.addFilm(film);
    }

    public Film updateFilm(Film film) {
        filmStorage.getById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм не найден: " + film.getId()));
        validateMpaAndGenres(film);
        return filmStorage.updateFilm(film);
    }

    public List<Film> getAll() {
        return filmStorage.getAll();
    }

    public Film getFilmById(int id) {
        return filmStorage.getById(id).orElseThrow(() -> new NotFoundException("Фильм не найден: " + id));
    }

    public void deleteFilm(int id) {
        filmStorage.getById(id).orElseThrow(() -> new NotFoundException("Фильм не найден: " + id));
        filmStorage.delete(id);
    }

    public void addLike(int filmId, int userId) {
        filmStorage.getById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден: " + filmId));
        userStorage.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(int filmId, int userId) {
        filmStorage.getById(filmId).orElseThrow(() -> new NotFoundException("Фильм не найден: " + filmId));
        userStorage.getById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getTopFilms(int count) {
        return filmStorage.getPopular(count);
    }

    private void validateMpaAndGenres(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new NotFoundException("Рейтинг MPA не указан");
        }

        mpaStorage.getById(film.getMpa().getId()).orElseThrow(() -> new NotFoundException("Рейтинг MPA не найден: " + film.getMpa().getId()));

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                genreStorage.getById(genre.getId()).orElseThrow(() -> new NotFoundException("Жанр не найден: " + genre.getId()));
            }
        }
    }
}