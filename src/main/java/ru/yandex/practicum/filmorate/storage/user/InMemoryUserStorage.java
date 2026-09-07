package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Override
    public User addUser(User user) {
        int id = idGenerator.getAndIncrement();
        user.setId(id);
        users.put(id, user);
        log.info("Добавлен пользователь: {}", user);
        return user;
    }

    @Override
    public User updateUser(User user) {
        Integer id = user.getId();
        if (id == null) {
            log.warn("Попытка обновить пользователя без id");
            throw new IllegalArgumentException("Id должен быть указан");
        }
        User existingUser = users.get(id);
        if (existingUser == null) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }

        existingUser.setEmail(user.getEmail());
        existingUser.setLogin(user.getLogin());
        existingUser.setName(user.getName());
        existingUser.setBirthday(user.getBirthday());

        log.info("Обновлён пользователь: {}", existingUser);
        return existingUser;
    }

    @Override
    public boolean delete(int id) {
        if (!users.containsKey(id)) {
            log.warn("Попытка удалить несуществующего пользователя с id {}", id);
            return false;
        }
        users.remove(id);
        log.info("Удалён пользователь с id {}", id);
        return true;
    }

    @Override
    public User getById(int id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("Запрошен несуществующий пользователь с id {}", id);
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        return user;
    }

    @Override
    public Collection<User> getAll() {
        return users.values();
    }

    public void clear() {
        users.clear();
        idGenerator.set(1);
    }
}
