package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getAll() {
        log.info("Запрос всех фильмов");
        return filmService.getAll();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        log.info("Добавление фильма: {}", film);
        return filmService.addFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        log.info("Обновление фильма: {}", film);
        return filmService.updateFilm(film);
    }

    @PutMapping("/{filmId}/like/{userId}")
    public void addLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Пользователь {} ставит лайк фильму {}", userId, filmId);
        filmService.addLike(filmId, userId);
    }

    @DeleteMapping("/{filmId}/like/{userId}")
    public void removeLike(@PathVariable int filmId, @PathVariable int userId) {
        log.info("Пользователь {} убирает лайк с фильма {}", userId, filmId);
        filmService.removeLike(filmId, userId);
    }

    @GetMapping("/popular")
    public List<Film> getTopFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("Запрос топ-{} фильмов", count);
        return filmService.getTopFilms(count);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable int id) {
        log.info("Запрос фильма с id {}", id);
        return filmService.getFilmById(id);
    }

}