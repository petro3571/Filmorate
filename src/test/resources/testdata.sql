INSERT INTO users (email, name, birthday, login)
VALUES ('user1@example.com', 'Alice Smith', '1990-05-15', 'alices'),
       ('user2@example.com', 'Bob Johnson', '1985-12-20', 'bobj'),
       ('user3@example.com', 'Charlie Brown', '1992-08-03', 'charlieb'),
       ('user4@example.com', 'Diana Miller', '1988-03-10', 'dianam'),
       ('user5@example.com', 'Ethan Davis', '1995-11-28', 'ethand'),
       ('user6@example.com', 'Kate Miller', '1989-03-10', 'kate');

INSERT INTO films (title, description, release_date, duration, mpa_id)
VALUES ('Film 1', 'Description 1', '2023-01-15', 120, 1),
       ('Film 2', 'Description 2', '2022-11-20', 95, 2),
       ('Film 3', 'Description 3', '2023-03-01', 150, 3),
       ('Film 4', 'Description 4', '2021-09-10', 110, 1),
       ('Film 5', 'Description 5', '2023-06-05', 135, 2),
       ('Film 6', 'Description 6', '2022-04-12', 88, 3),
       ('Film 7', 'Description 7', '2023-08-22', 142, 1),
       ('Film 8', 'Description 8', '2021-12-31', 105, 2),
       ('Film 9', 'Description 9', '2023-02-18', 160, 3),
       ('Film 10', 'Description 10', '2022-07-07', 92, 1),
       ('Film 11', 'Description 11', '2023-02-18', 160, 3);

INSERT INTO likes (film_id, user_id)
VALUES (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (1, 5),

       (2, 1),
       (2, 2),
       (2, 3),
       (2, 4),
       (2, 5),

       (3, 1),
       (3, 2),
       (3, 3),
       (3, 4),

       (4, 1),
       (4, 2),
       (4, 3),
       (4, 4),

       (5, 1),
       (5, 2),
       (5, 3),

       (6, 1),
       (6, 3),
       (6, 5),

       (7, 1),
       (7, 5),

       (8, 3),

       (9, 1),
       (9, 4),

       (11, 1),
       (11, 2),
       (11, 3),
       (11, 4),
       (11, 5),
       (11, 6);

INSERT INTO film_genre (film_id, genre_id)
VALUES (1, 1),
       (1, 6),
       (2, 2),
       (3, 1),
       (4, 2),
       (5, 2),
       (6, 1),
       (7, 4),
       (8, 1),
       (9, 3),
       (10, 3),
       (11, 1);

INSERT INTO reviews (user_id, film_id, positive, content) VALUES
                                                              (1, 1, true, 'Отличный фильм! Актерская игра на высоте, сюжет захватывающий. Рекомендую всем любителям жанра.'),
                                                              (2, 1, false, 'Не оправдал ожиданий. Сюжет предсказуем, спецэффекты выглядят дешево.'),
                                                              (3, 2, true, 'Настоящий шедевр! Потрясающая операторская работа и глубокая философская подоплека.'),
                                                              (1, 3, true, 'Легкий и приятный фильм для вечернего просмотра. Хороший юмор и теплая атмосфера.'),
                                                              (4, 2, false, 'Слишком затянуто. Интересные моменты тонут в море ненужных диалогов.');

INSERT INTO review_likes (review_id, user_id) VALUES (1, 2),
                                                     (1, 3),

                                                     (2, 1),
                                                     (2, 3),
                                                     (2, 4),

                                                     (3, 1),

                                                     (5, 1),
                                                     (5, 2),
                                                     (5, 3),
                                                     (5, 5);

INSERT INTO review_dislikes (review_id, user_id) VALUES (1, 4),--рейтинг 1
                                                        (3, 2),
                                                        (3, 5);

