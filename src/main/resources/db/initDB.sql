DROP TABLE IF EXISTS render_users CASCADE;
DROP TABLE IF EXISTS users_tasks;
DROP SEQUENCE IF EXISTS global_seq;

CREATE SEQUENCE global_seq START 1;

CREATE TABLE render_users
(
  id INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
  email VARCHAR NOT NULL,
  password VARCHAR NOT NULL
);

CREATE UNIQUE INDEX users_unique_email_idx ON render_users (email);

CREATE TABLE users_tasks
(
  id INTEGER PRIMARY KEY DEFAULT nextval('global_seq'),
  user_id INTEGER NOT NULL,
  status VARCHAR,
  time_created TIMESTAMP DEFAULT now()  NOT NULL,
  FOREIGN KEY (user_id) REFERENCES render_users (id) ON DELETE CASCADE
);

