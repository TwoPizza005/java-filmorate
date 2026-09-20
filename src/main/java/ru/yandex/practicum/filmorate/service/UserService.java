package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        userStorage.getById(user.getId())
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + user.getId()));
        return userStorage.updateUser(user);
    }

    public List<User> getAll() {
        return userStorage.getAll();
    }

    public User getUserById(int id) {
        return userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
    }

    public void deleteUser(int id) {
        userStorage.getById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + id));
        userStorage.delete(id);
    }

    public void addFriend(int userId, int friendId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        userStorage.getById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + friendId));

        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить в друзья самого себя");
        }

        friendshipStorage.addFriend(userId, friendId);
        log.info("Заявка/подтверждение дружбы: {} <-> {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        userStorage.getById(friendId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + friendId));

        friendshipStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(int userId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        return friendshipStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        userStorage.getById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + userId));
        userStorage.getById(otherId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден: " + otherId));
        return friendshipStorage.getCommonFriends(userId, otherId);
    }
}