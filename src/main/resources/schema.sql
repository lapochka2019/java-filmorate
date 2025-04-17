-- Удаление зависимых таблиц
DROP TABLE IF EXISTS film_likes;
DROP TABLE IF EXISTS film_genres;
DROP TABLE IF EXISTS user_friends;

-- Удаление таблиц с внешними ключами
DROP TABLE IF EXISTS films;
DROP TABLE IF EXISTS users;

-- Удаление независимых таблиц
DROP TABLE IF EXISTS mpa_rating;
DROP TABLE IF EXISTS genre;
DROP TABLE IF EXISTS friendship_type;


-- Создание таблицы mpa Rating
CREATE TABLE IF NOT EXISTS mpa_rating (
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
CREATE TABLE IF NOT EXISTS users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    login VARCHAR(100) NOT NULL,
    name VARCHAR(255),
    birthday DATE
);

-- Создание таблицы Film
CREATE TABLE IF NOT EXISTS films (
    id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    release_date DATE NOT NULL,
    duration INT NOT NULL,
    mpa_rating_id INT,
    rate INT DEFAULT 0,--likes count
    FOREIGN KEY (mpa_rating_id) REFERENCES mpa_rating(id) --mpa rating from rating table
);

-- Создание таблицы Film_Likes
CREATE TABLE IF NOT EXISTS film_likes (
    film_id INT,
    user_id INT,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Создание таблицы Film_Genre
CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT,
    genre_id INT,
    PRIMARY KEY (film_id, genre_id),
    FOREIGN KEY (film_id) REFERENCES films(id),
    FOREIGN KEY (genre_id) REFERENCES genre(id)
);

-- Создание таблицы User_Friends
CREATE TABLE IF NOT EXISTS user_friends (
    user_id INT,
    friend_id INT,
    friendship_type_id INT,
    PRIMARY KEY (user_id, friend_id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (friend_id) REFERENCES users(id),
    FOREIGN KEY (friendship_type_id) REFERENCES friendship_type(id)
);