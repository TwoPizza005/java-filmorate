package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getAll() {
        log.info("Запрос всех пользователей");
        return userService.getAll();
    }

    @PostMapping
    public User addUser(@Valid @RequestBody User user) {
        // Заменяем пустое имя на логин
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Добавление пользователя: {}", user);
        return userService.addUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        log.info("Обновление пользователя: {}", user);
        return userService.updateUser(user);
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addFriend(@PathVariable int userId, @PathVariable int friendId) {
        log.info("Пользователь {} добавляет в друзья {}", userId, friendId);
        userService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{userId}/friends/{friendId}")
    public void removeFriend(@PathVariable int userId, @PathVariable int friendId) {
        log.info("Пользователь {} удаляет из друзей {}", userId, friendId);
        userService.removeFriend(userId, friendId);
    }

    @GetMapping("/{userId}/friends")
    public List<User> getFriends(@PathVariable int userId) {
        log.info("Запрос друзей пользователя {}", userId);
        return userService.getFriends(userId);
    }

    @GetMapping("/{userId}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable int userId, @PathVariable int otherId) {
        log.info("Запрос общих друзей пользователей {} и {}", userId, otherId);
        return userService.getCommonFriends(userId, otherId);
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable int id) {
        log.info("Запрос пользователя с id {}", id);
        return userService.getUserById(id);
    }
}