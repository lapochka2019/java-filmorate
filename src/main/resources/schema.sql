-- Удаление зависимых таблиц
--DROP TABLE IF EXISTS film_likes;
--DROP TABLE IF EXISTS film_genre;
--DROP TABLE IF EXISTS user_friends;

-- Удаление таблиц с внешними ключами
--DROP TABLE IF EXISTS film;
--DROP TABLE IF EXISTS consumer;

-- Удаление независимых таблиц
--DROP TABLE IF EXISTS rating;
--DROP TABLE IF EXISTS genre;
--DROP TABLE IF EXISTS friendship_type;


-- Создание таблицы Rating
CREATE TABLE IF NOT EXISTS rating (
    id INT PRIMARY KEY,
    name VARCHAR(20) NOT NULL
);

-- Создание таблицы Genre
CREATE TABLE IF NOT EXISTS genre (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Создание таблицы Friendship_Type
CREATE TABLE IF NOT EXISTS friendship_type (
    id INT PRIMARY KEY,
    name VARCHAR(50) NOT NULL
);

-- Создание таблицы Consumer
CREATE TABLE IF NOT EXISTS consumer (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(100) NOT NULL,
    name VARCHAR(255),
    birthday DATE
);

-- Создание таблицы Film
CREATE TABLE IF NOT EXISTS film (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    rating_id INT,
    FOREIGN KEY (rating_id) REFERENCES rating(id)
);

-- Создание таблицы Film_Likes
CREATE TABLE IF NOT EXISTS film_likes (
    film_id BIGINT,
    user_id BIGINT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES film(id),
    FOREIGN KEY (user_id) REFERENCES consumer(id)
);

-- Создание таблицы Film_Genre
CREATE TABLE IF NOT EXISTS film_genre (
    film_id BIGINT,
    genre_id INT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES film(id),
    FOREIGN KEY (genre_id) REFERENCES genre(id)
);

-- Создание таблицы User_Friends
CREATE TABLE IF NOT EXISTS user_friends (
    user_id BIGINT,
    friend_id BIGINT,
    friendship_type_id INT,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES consumer(id),
    FOREIGN KEY (friend_id) REFERENCES consumer(id),
    FOREIGN KEY (friendship_type_id) REFERENCES friendship_type(id)
);