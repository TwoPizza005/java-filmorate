package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }

    public Collection<User> getAll() {
        return userStorage.getAll();
    }

    public void addFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
        log.info("Пользователи {} и {} стали друзьями", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        User user = userStorage.getById(userId);
        User friend = userStorage.getById(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
        log.info("Пользователи {} и {} перестали быть друзьями", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        User user = userStorage.getById(userId);
        return user.getFriends().stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        User user = userStorage.getById(userId);
        User other = userStorage.getById(otherId);
        Set<Integer> common = new HashSet<>(user.getFriends());
        common.retainAll(other.getFriends());
        return common.stream()
                .map(userStorage::getById)
                .collect(Collectors.toList());
    }

    public User getUserById(int id) {
        return userStorage.getById(id);
    }
}
