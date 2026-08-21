package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private final Map<Integer, Film> films = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @GetMapping
    public Collection<Film> getAll() {
        return films.values();
    }

    @PostMapping
    public Film addFilm(@Valid @RequestBody Film film) {
        int id = idGenerator.getAndIncrement();
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        int id = film.getId();
        if (!films.containsKey(id)) {
            log.warn("Ошибка изменения фильма: {}", film);
            throw new ValidationException("Фильм с id " + id + " не найден");
        }
        films.put(id, film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }
}