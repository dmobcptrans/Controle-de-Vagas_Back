ALTER TABLE usuario
    drop column telefone,
    add column telefone_hash varchar(64),
    add column telefone_cripto text,
    add column telefone_last4 varchar(4),
    add column telefone_key_version int not null default 1;

ALTER TABLE motorista
    drop column numero_cnh,
    add column cnh_hash varchar(64),
    add column cnh_cripto text,
    add column cnh_last4 varchar(4),
    add column cnh_key_version int not null default 1;