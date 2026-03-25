alter table usuario
    drop column email,
    add column email_cripto VARCHAR(255),
    add column email_hash VARCHAR(64) UNIQUE,
    drop column telefone_key_version,
    drop column cpf_key_version,
    add column personal_data_key_version INT NOT NULL DEFAULT 1;

alter table motorista
    drop column cnh_key_version;