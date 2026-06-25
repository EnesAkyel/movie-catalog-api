CREATE TABLE studios (
    sid   INTEGER       PRIMARY KEY,
    name  VARCHAR(255)  NOT NULL
);

CREATE TABLE movies (
    mid       INTEGER          PRIMARY KEY,
    name      VARCHAR(255)     NOT NULL,
    genre     VARCHAR(50)      NOT NULL,
    price     DOUBLE PRECISION NOT NULL,
    rating    VARCHAR(10)      NOT NULL,
    studio_id INTEGER          NOT NULL
);
