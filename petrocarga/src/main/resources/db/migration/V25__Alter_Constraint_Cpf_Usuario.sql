UPDATE usuario 
    SET cpf_hash = NULL 
    WHERE cpf_hash = '';

UPDATE usuario
    SET cpf_cripto = NULL 
    WHERE cpf_cripto ='';

UPDATE usuario 
    SET cpf_last5 = NULL 
    WHERE cpf_last5 = '';

ALTER TABLE usuario
    ADD CONSTRAINT usuario_cpf_hash_key UNIQUE (cpf_hash);