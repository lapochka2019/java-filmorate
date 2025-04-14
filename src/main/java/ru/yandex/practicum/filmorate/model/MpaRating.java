package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MpaRating {
    //    целочисленный идентификатор
    private int id;
    //    название
    @NotBlank(message = "Имя не может быть пустым")
    private String name;

    public MpaRating(int id) {
        this.id = id;
    }
}
