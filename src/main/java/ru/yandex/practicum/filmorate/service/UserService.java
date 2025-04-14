package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;

@Slf4j
@Service
public class UserService {
    UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userStorage) {
        this.userRepository = userStorage;
    }

    //добавить в друзья
    public void addFriend(int firstId, int secondId) {
        log.info("Пользователь {} пытается добавить в друзья пользователя {}", firstId, secondId);
        userRepository.addFriend(firstId, secondId);
    }

    //удалить из друзей
    public void deleteFriend(int firstId, int secondId) {
        userRepository.deleteFriend(firstId, secondId);
    }

    //список друзей
    public List<UserDto> getFriends(int id) {
        log.info("Попытка получить друзей пользователя {}", id);
        return userRepository.getFriends(id);
    }

    //список общих друзей
    public List<UserDto> getMutualFriends(int firstId, int secondId) {
        log.info("Попытка получить общих друзей пользователей {} и {}", firstId, secondId);
        return userRepository.getMutualFriends(firstId, secondId);
    }

    public void addUser(User user) {
        log.info("Попытка добавить пользователя с id:{}", user.getId());
        userRepository.addUser(user);
    }

    public void updateUser(User user) {
        log.info("Попытка обновить пользователя с id:{}", user.getId());
        userRepository.updateUser(user);
    }

    public UserDto getUser(int id) {
        log.info("Попытка получить пользователя с id:{}", id);
        return userRepository.getUser(id);
    }

    public List<UserDto> getUsers() {
        log.info("Попытка получить всех пользователей");
        return userRepository.getUsers();
    }
}
