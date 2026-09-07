package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> getAll();

    User addUser(User user);

    User updateUser(User user);

    boolean delete(int id);

    User getById(int id);
}
