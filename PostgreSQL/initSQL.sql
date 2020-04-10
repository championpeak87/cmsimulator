CREATE DATABASE cmsimulator;

\c cmsimulator;

CREATE TYPE automata_type AS ENUM
    ('finite_automata', 'pushdown_automata', 'linear_bounded_automata', 'turing_machine');

CREATE TYPE task_status AS ENUM
    ('new', 'correct', 'wrong', 'in_progress', 'too_late');

CREATE TYPE user_type AS ENUM
    ('admin', 'lector', 'student');

CREATE TABLE users
(
    user_id serial NOT NULL unique,
    user_type user_type NOT NULL default('student'),
    username text NOT NULL unique CHECK(length(username) >= 6),
    first_name text NOT NULL,
    last_name text NOT NULL,
    password_hash bytea NOT NULL,
    salt bytea NOT NULL,
    PRIMARY KEY (user_id)
);

CREATE TABLE automata_tasks
(
    task_id serial NOT NULL unique,
    assigner_id serial NOT NULL references users(user_id),
    task_name text not null,
    task_description text,
    time interval not null default('00:00:00'),
    public_input boolean not null default(true),
    automata_type automata_type not null,
    PRIMARY KEY (task_id)
);

CREATE TABLE automata_task_results
(
    user_id serial not null references users(user_id),
    task_id serial not null references automata_tasks(task_id),
    task_status task_status not null default('new'),
    time_elapsed interval not null default('00:00:00'),
    submitted boolean not null default(false),
    submission_date timestamp
);

CREATE TABLE grammar_tasks
(
    task_id serial NOT NULL unique,
    assigner_id serial NOT NULL references users(user_id),
    task_name text not null,
    task_description text,
    time interval not null default('00:00:00'),
    public_input boolean not null default(true),
    PRIMARY KEY (task_id)
);

CREATE TABLE grammar_task_results
(
    user_id serial not null references users(user_id),
    task_id serial not null references grammar_tasks(task_id),
    task_status task_status not null default('new'),
    time_elapsed interval not null default('00:00:00'),
    submitted boolean not null default(false),
    submission_date timestamp
);

CREATE TABLE game_tasks
(
    task_id SERIAL NOT NULL UNIQUE,
    assigner_id SERIAL NOT NULL REFERENCES users(user_id),
    task_name text not null,
    task_description text,
    automata_type automata_type not null,
    primary key (task_id)
);