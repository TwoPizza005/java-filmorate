package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.NoSpaces;

import java.time.LocalDate;

@Data
public class User {
    private Integer id;
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Некорректный формат email")
    private String email;
    @NotBlank(message = "Логин не может быть пустым")
    @NoSpaces(message = "Логин не должен содержать пробелы")
    private String login;
    private String name;
    @NotNull(message = "Дата рождения обязательна")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
