CREATE TABLE IF NOT EXISTS users (
            user_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            email VARCHAR(255) NOT NULL,
            name VARCHAR(40) NOT NULL,
            birthday DATE NOT NULL,
            login VARCHAR(40) NOT NULL
          );

CREATE TABLE IF NOT EXISTS MPA (
            id INT PRIMARY KEY,
            name VARCHAR(40) NOT NULL
            );

 CREATE TABLE IF NOT EXISTS films (
            film_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            title VARCHAR(255) NOT NULL,
            description VARCHAR(255) NOT NULL,
            release_date DATE NOT NULL,
            duration INTEGER NOT NULL,
            MPA_id INTEGER REFERENCES mpa ON DELETE SET NULL
          );

CREATE TABLE IF NOT EXISTS genre (
            id INT PRIMARY KEY,
            name VARCHAR(40) NOT NULL
            );


CREATE TABLE IF NOT EXISTS film_genre (
            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            film_id INTEGER REFERENCES films ON DELETE CASCADE,
            genre_id INTEGER REFERENCES genre ON DELETE CASCADE
            );

CREATE TABLE IF NOT EXISTS likes (
			id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            film_id INTEGER REFERENCES films ON DELETE CASCADE,
            user_id INTEGER REFERENCES users ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS friends (
            id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
            user_id INTEGER REFERENCES users ON DELETE CASCADE,
            friend_id INTEGER REFERENCES users ON DELETE CASCADE,
            friend_confirm BOOLEAN DEFAULT FALSE
            );