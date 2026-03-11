ALTER TABLE usuario
    alter column cpf_hash drop not null,
	alter column cpf_cripto drop not null,
	alter column cpf_last5 drop not null,
	alter column senha drop not null,
	alter column ativo set not null,
    add column google_id varchar(255) unique,
    add column provider varchar(20) default 'LOCAL';