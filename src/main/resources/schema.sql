CREATE SCHEMA IF NOT EXISTS bookshelf;

/*ユーザマスタ*/
CREATE TABLE IF NOT EXISTS bookshelf.m_user (
    id   text PRIMARY KEY,
    name text,
    mail_address text,
    role_name text,
    password text
);

/*ジャンルマスタ*/
CREATE TABLE IF NOT EXISTS bookshelf.m_genre (
    id  integer PRIMARY KEY,
    name text
);

/*本情報*/
CREATE TABLE IF NOT EXISTS bookshelf.t_book (
    user_id text,
    seq_no integer,
    author text,
    title text,
    buy_date date,
    complete_date date,
    genre_id integer,
    img_url text,
    info_url text,
    memo text,
    price integer,
    published date,
    publisher text,
    rate integer,
    PRIMARY KEY (user_id, seq_no) 
);
