package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @GetMapping
    public List<User> getAll() {
        log.info("Запрос на получение всех пользователей");
        return new ArrayList<>(users.values());
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        log.info("Получен запрос на добавление пользователя: {}", user);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, заменено на логин: {}", user.getLogin());
        }
        int id = idGenerator.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        log.info("Пользователь успешно добавлен с id {}: {}", id, user);
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        int id = user.getId();
        log.info("Получен запрос на обновление пользователя с id {}: {}", id, user);
        if (!users.containsKey(id)) {
            log.warn("Попытка обновить несуществующего пользователя с id {}", id);
            throw new ValidationException("Пользователь с id " + id + " не найден");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, заменено на логин: {}", user.getLogin());
        }
        users.put(id, user);
        log.info("Пользователь с id {} успешно обновлён: {}", id, user);
        return user;
    }
}
