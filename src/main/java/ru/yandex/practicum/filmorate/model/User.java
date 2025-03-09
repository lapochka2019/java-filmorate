package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    //    целочисленный идентификатор
    private int id;
    //    электронная почта
    @NotBlank(message = "Email не может быть пустым")
    @Email(message = "Это не похоже на email")
    private String email;
    //    логин пользователя
    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должен содержать пробелы")
    private String login;
    //    имя для отображения
    private String name;
    //    дата рождения
    @Past(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
    //список с id пользователей-друзей
    private Set<Integer> friends = new HashSet<>();

    public void addFriend(int id) {
        friends.add(id);
    }

    public void deleteFriend(int id) {
        friends.remove(id);
    }
}
