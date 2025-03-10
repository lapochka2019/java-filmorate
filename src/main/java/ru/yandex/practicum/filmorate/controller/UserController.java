package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    UserService userService;

    @Autowired
    public UserController(UserService service) {
        this.userService = service;
    }

    //    добавление пользователя
    @PostMapping()
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        log.info("Успешное создание нового пользователя: {}", user);
        userService.addUser(user);
        return ResponseEntity.ok(user);
    }

    //    обновление пользователей
    @PutMapping()
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        log.info("Успешное обновление пользователя: {}", user);
        userService.updateUser(user);
        return ResponseEntity.ok(user);
    }

    //получить 1 пользователя
    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(@PathVariable int id) {
        log.info("Получение пользователя по id");
        return ResponseEntity.ok(userService.getUser(id));
    }

    //    получение всех пользователей
    @GetMapping()
    public ResponseEntity<List<User>> getUsers() {
        log.info("Получение всех пользователей");
        return ResponseEntity.ok(userService.getUsers());
    }

    @PutMapping("/{id}/friends/{friendId}")
    public ResponseEntity<?> addFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        log.info("Добавление в друзья");
        userService.addFriend(userId, friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public ResponseEntity<?> deleteFriend(@PathVariable("id") int userId, @PathVariable int friendId) {
        log.info("Удаление из друзей");
        userService.deleteFriend(userId, friendId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/{id}/friends")
    public ResponseEntity<List<User>> getFriends(@PathVariable int id) {
        log.info("Получение всех друзей пользователя с id = {}", id);
        return ResponseEntity.ok(userService.getFriends(id));
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public ResponseEntity<List<User>> getFriends(@PathVariable int id, @PathVariable int otherId) {
        log.info("Получение общих друзей пользователей с id {} и {}", id, otherId);
        return ResponseEntity.ok(userService.getMutualFriends(id, otherId));
    }
}
