MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (1, 'Комедия');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (2, 'Драма');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (3, 'Мультфильм');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (4, 'Триллер');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (5, 'Документальный');
MERGE INTO genre (genre_id, name) KEY(genre_id) VALUES (6, 'Боевик');

MERGE INTO mpa_rating (mpa_id, name) KEY(mpa_id) VALUES (1, 'G');
MERGE INTO mpa_rating (mpa_id, name) KEY(mpa_id) VALUES (2, 'PG');
MERGE INTO mpa_rating (mpa_id, name) KEY(mpa_id) VALUES (3, 'PG-13');
MERGE INTO mpa_rating (mpa_id, name) KEY(mpa_id) VALUES (4, 'R');
MERGE INTO mpa_rating (mpa_id, name) KEY(mpa_id) VALUES (5, 'NC-17');