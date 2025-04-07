MERGE INTO rating (id, name) KEY (id) VALUES
(1, 'G'),
(2, 'PG'),
(3, 'PG-13'),
(4, 'R'),
(5, 'NC-17');

MERGE INTO genre (id, name) KEY (id) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');

MERGE INTO friendship_type (id, name) KEY (id) VALUES
(1,'Не подтверждена'),
(2,'Подтверждена');

INSERT INTO consumer (email, login, name, birthday) VALUES
('user1@example.com', 'user1_login', 'User One', '1990-05-15'),
('user2@example.com', 'user2_login', 'User Two', '1985-12-25'),
('user3@example.com', 'user3_login', 'User Three', '1995-07-20'),
('user4@example.com', 'user4_login', 'User Four', '2000-03-10'),
('user5@example.com', 'user5_login', 'User Five', '1980-09-30');

DELETE FROM user_friends;
INSERT INTO user_friends (user_id, friend_id, friendship_type_id) VALUES
(1, 2, 2), -- User One и User Two (дружба подтверждена)
(1, 3, 1), -- User One и User Three (дружба не подтверждена)
(2, 4, 2), -- User Two и User Four (дружба подтверждена)
(3, 5, 1), -- User Three и User Five (дружба не подтверждена)
(4, 5, 2); -- User Four и User Five (дружба подтверждена)

INSERT INTO film (name, description, release_date, duration, rating_id) VALUES
('Фильм 1', 'Описание фильма 1', '2020-01-15', 120, 1), -- Рейтинг G
('Фильм 2', 'Описание фильма 2', '2019-06-20', 95, 2),  -- Рейтинг PG
('Фильм 3', 'Описание фильма 3', '2021-03-10', 150, 3), -- Рейтинг PG-13
('Фильм 4', 'Описание фильма 4', '2018-11-05', 110, 4), -- Рейтинг R
('Фильм 5', 'Описание фильма 5', '2022-07-25', 130, 5); -- Рейтинг NC-17

DELETE FROM film_genre;
INSERT INTO film_genre (film_id, genre_id) VALUES
(1, 1), -- Фильм 1 связан с жанром "Комедия"
(1, 2), -- Фильм 1 также связан с жанром "Драма"
(2, 3), -- Фильм 2 связан с жанром "Мультфильм"
(3, 4), -- Фильм 3 связан с жанром "Триллер"
(4, 5), -- Фильм 4 связан с жанром "Документальный"
(5, 6), -- Фильм 5 связан с жанром "Боевик"
(5, 1); -- Фильм 5 также связан с жанром "Комедия"

DELETE FROM film_likes;
INSERT INTO film_likes (film_id, user_id) VALUES
(1, 1), -- Пользователь 1 поставил лайк Фильму 1
(1, 2), -- Пользователь 2 поставил лайк Фильму 1
(2, 3), -- Пользователь 3 поставил лайк Фильму 2
(3, 1), -- Пользователь 1 поставил лайк Фильму 3
(4, 4), -- Пользователь 4 поставил лайк Фильму 4
(5, 5), -- Пользователь 5 поставил лайк Фильму 5
(3, 5); -- Пользователь 5 поставил лайк Фильму 3