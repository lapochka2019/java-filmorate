package ru.yandex.practicum.filmorate.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.MpaRepository;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class MpaService {

    MpaRepository mpaRepository;

    public MpaRating getMpa(int id) {
        log.info("Попытка получить МРА рейтинг с id:{}", id);
        return mpaRepository.getMpa(id);
    }

    public List<FilmDto> getFilmsWithMpa() {
        log.info("Попытка получить фильмы с МРА рейтингом");
        return mpaRepository.getFilmsWithMpa();
    }
}
