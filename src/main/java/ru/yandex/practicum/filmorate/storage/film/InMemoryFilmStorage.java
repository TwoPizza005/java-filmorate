package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Integer, Film> films = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public Film addFilm(Film film) {
        int id = idGenerator.getAndIncrement();
        film.setId(id);
        films.put(id, film);
        log.info("Добавлен фильм: {}", film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        Integer id = film.getId();
        if (id == null) {
            log.warn("Попытка обновить фильм без id");
            throw new IllegalArgumentException("Id должен быть указан");
        }
        if (!films.containsKey(id)) {
            log.warn("Фильм с id {} не найден", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        films.put(id, film);
        log.info("Обновлён фильм: {}", film);
        return film;
    }

    @Override
    public boolean delete(int id) {
        if (!films.containsKey(id)) {
            log.warn("Попытка удалить несуществующий фильм с id {}", id);
            return false;
        }
        films.remove(id);
        log.info("Удалён фильм с id {}", id);
        return true;
    }

    @Override
    public Film getById(int id) {
        Film film = films.get(id);
        if (film == null) {
            log.warn("Запрошен несуществующий фильм с id {}", id);
            throw new NotFoundException("Фильм с id " + id + " не найден");
        }
        return film;
    }

    @Override
    public Collection<Film> getAll() {
        return films.values();
    }

    //метод для чистки, возможно вызвать ток в тестах
    public void clear() {
        films.clear();
        idGenerator.set(1);
    }
}