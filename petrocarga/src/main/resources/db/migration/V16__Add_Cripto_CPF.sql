alter table usuario 
	drop column cpf,
	add column cpf_hash varchar(64) not null default '',
	add column cpf_cripto text not null default '',
	add column cpf_key_version int not null default 1,
 	add column cpf_last5 varchar(5) not null default '';

alter table veiculo
	drop column cpf_proprietario,
	add column cpf_proprietario_hash varchar(64),
	add column cpf_proprietario_cripto text,
	add column cpf_proprietario_key_version int,
 	add column cpf_proprietario_last5 varchar(5);