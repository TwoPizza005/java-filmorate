package ru.yandex.practicum.filmorate.storage.friendship;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FriendshipDbStorage.class, UserRowMapper.class, UserDbStorage.class})
class FriendshipDbStorageTest {

    private final FriendshipDbStorage friendshipStorage;
    private final UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc.update("DELETE FROM likes");
        jdbc.update("DELETE FROM film_genre");
        jdbc.update("DELETE FROM friendship");
        jdbc.update("DELETE FROM film");
        jdbc.update("DELETE FROM users");
    }

    private User makeUser(String email, String login) {
        User user = new User();
        user.setEmail(email);
        user.setLogin(login);
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.addUser(user);
    }

    @Test
    void testAddFriendCreatesUnconfirmed() {
        User user = makeUser("a@mail.ru", "a");
        User friend = makeUser("b@mail.ru", "b");

        friendshipStorage.addFriend(user.getId(), friend.getId());

        List<User> friends = friendshipStorage.getFriends(user.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(friend.getId());

        assertThat(friendshipStorage.getFriends(friend.getId())).isEmpty();
    }

    @Test
    void testMutualAddConfirmsFriendship() {
        User user = makeUser("a@mail.ru", "a");
        User friend = makeUser("b@mail.ru", "b");

        friendshipStorage.addFriend(user.getId(), friend.getId());
        friendshipStorage.addFriend(friend.getId(), user.getId());

        assertThat(friendshipStorage.getFriends(user.getId())).hasSize(1);
        assertThat(friendshipStorage.getFriends(friend.getId())).hasSize(1);

        String status = jdbc.queryForObject(
                "SELECT friend_status FROM friendship WHERE user_id = ? AND friend_id = ?",
                String.class, user.getId(), friend.getId());
        assertThat(status).isEqualTo("CONFIRMED");
    }

    @Test
    void testAddFriendTwiceDoesNothing() {
        User user = makeUser("a@mail.ru", "a");
        User friend = makeUser("b@mail.ru", "b");

        friendshipStorage.addFriend(user.getId(), friend.getId());
        friendshipStorage.addFriend(user.getId(), friend.getId());

        assertThat(friendshipStorage.getFriends(user.getId())).hasSize(1);
    }

    @Test
    void testRemoveFriend() {
        User user = makeUser("a@mail.ru", "a");
        User friend = makeUser("b@mail.ru", "b");

        friendshipStorage.addFriend(user.getId(), friend.getId());
        friendshipStorage.removeFriend(user.getId(), friend.getId());

        assertThat(friendshipStorage.getFriends(user.getId())).isEmpty();
    }

    @Test
    void testRemoveConfirmedFriend() {
        User user = makeUser("a@mail.ru", "a");
        User friend = makeUser("b@mail.ru", "b");

        friendshipStorage.addFriend(user.getId(), friend.getId());
        friendshipStorage.addFriend(friend.getId(), user.getId());
        friendshipStorage.removeFriend(user.getId(), friend.getId());

        assertThat(friendshipStorage.getFriends(user.getId())).isEmpty();
        assertThat(friendshipStorage.getFriends(friend.getId())).isEmpty();
    }

    @Test
    void testGetFriendsEmpty() {
        User user = makeUser("a@mail.ru", "a");
        assertThat(friendshipStorage.getFriends(user.getId())).isEmpty();
    }

    @Test
    void testGetFriendsMultiple() {
        User user = makeUser("a@mail.ru", "a");
        User f1 = makeUser("b@mail.ru", "b");
        User f2 = makeUser("c@mail.ru", "c");

        friendshipStorage.addFriend(user.getId(), f1.getId());
        friendshipStorage.addFriend(user.getId(), f2.getId());

        List<User> friends = friendshipStorage.getFriends(user.getId());
        assertThat(friends).hasSize(2);
        assertThat(friends).extracting(User::getId)
                .containsExactlyInAnyOrder(f1.getId(), f2.getId());
    }

    @Test
    void testGetCommonFriends() {
        User user1 = makeUser("u1@mail.ru", "u1");
        User user2 = makeUser("u2@mail.ru", "u2");
        User common = makeUser("c@mail.ru", "c");
        User onlyFirst = makeUser("o1@mail.ru", "o1");

        friendshipStorage.addFriend(user1.getId(), common.getId());
        friendshipStorage.addFriend(user2.getId(), common.getId());
        friendshipStorage.addFriend(user1.getId(), onlyFirst.getId());

        List<User> commonFriends = friendshipStorage.getCommonFriends(user1.getId(), user2.getId());
        assertThat(commonFriends)
                .hasSize(1)
                .extracting(User::getId)
                .containsExactly(common.getId());
    }

    @Test
    void testGetCommonFriendsEmpty() {
        User user1 = makeUser("u1@mail.ru", "u1");
        User user2 = makeUser("u2@mail.ru", "u2");

        assertThat(friendshipStorage.getCommonFriends(user1.getId(), user2.getId())).isEmpty();
    }
}