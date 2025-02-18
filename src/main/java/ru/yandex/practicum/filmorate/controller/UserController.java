package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
@Validated
public class UserController {
    public final Map<Integer, User> users;
    private int id = 1;

    public UserController() {
        this.users = new HashMap<>();
    }

    //    добавление пользователя
    @PostMapping()
    public ResponseEntity<?> createUser(@Valid @RequestBody User user) {
        //Не стала делать проверку на пустую сущность
        //так как предусмотрена валидация и хоть какие-то данные должны быть
        putUserName(user);
        addId(user);
        log.info("Успешное создание нового пользователя: {}", user);
        users.put(user.getId(), user);
        return ResponseEntity.ok(user);
    }

    //    обновление пользователей
    @PutMapping()
    public ResponseEntity<?> updateUser(@Valid @RequestBody User user) {
        //Не стала делать проверку на пустую сущность
        //так как предусмотрена валидация и хоть какие-то данные должны быть
        //проверяем, что такой пользователь уже есть в "БД" и
        if (!users.containsKey(user.getId())) {
            log.warn("Попытка обновить несуществующего пользователя");
            throw new NotFoundException("Такой пользователь не найден!");
        }
        putUserName(user);
        log.info("Успешное обновление пользователя: {}", user);
        users.put(user.getId(), user);
        return ResponseEntity.ok(user);
    }

    //    получение всех пользователей
    @GetMapping()
    public ResponseEntity<List<User>> getUsers() {
        log.info("Получение всех пользователей");
        return ResponseEntity.ok(new ArrayList<>(users.values()));
    }

    //заполнить имя пользователя
    private void putUserName(User user) {
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
            log.info("Имя пользователя заполнено, как логин");
        }
    }

    private void addId(User user) {
        user.setId(id);
        id++;
    }
}
