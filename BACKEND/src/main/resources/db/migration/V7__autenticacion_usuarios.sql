ALTER TABLE usuarios
    ADD COLUMN username VARCHAR(80);

ALTER TABLE usuarios
    ADD COLUMN password_hash VARCHAR(100);

CREATE UNIQUE INDEX uq_usuarios_username ON usuarios (username);
