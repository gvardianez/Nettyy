create table if not exists users
(
    idUsers         bigserial primary key,
    nickname        varchar(255) not null unique ,
    login           varchar(255) not null unique ,
    password        varchar(45) not null
);

create table if not exists share
(
    idShare         bigserial primary key,
    idUser          int not null references users (idUsers),
    filename        TEXT not null ,
    filepath        TEXT not null unique
);