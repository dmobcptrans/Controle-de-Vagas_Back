-- empresa
ALTER TABLE IF EXISTS empresa
    DROP COLUMN IF EXISTS razao_social;

ALTER table IF EXISTS motorista
    DROP CONSTRAINT IF EXISTS fk9sqkske5wgplsv3f9whkapta8,
    DROP CONSTRAINT IF EXISTS motorista_empresa_id_fkey;

update motorista m
    SET empresa_id = e.usuario_id
        FROM empresa e
            WHERE m.empresa_id  = e.id;

UPDATE empresa
    SET id = usuario_id;

ALTER TABLE IF EXISTS empresa
    DROP CONSTRAINT IF EXISTS empresa_usuario_id_key;

ALTER TABLE IF EXISTS empresa
    DROP COLUMN IF EXISTS usuario_id;

ALTER TABLE IF EXISTS empresa
    DROP CONSTRAINT IF EXISTS empresa_id_fkey,
    DROP CONSTRAINT IF EXISTS empresa_usuario_id_key;

ALTER TABLE IF EXISTS empresa
	ADD constraint empresa_id_fkey
	FOREIGN KEY (id)
	REFERENCES usuario(id);


-- motorista

ALTER TABLE IF EXISTS motorista 
    ADD COLUMN IF NOT EXISTS cpf_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cpf_cripto VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cpf_last5 VARCHAR(5);

UPDATE motorista m
    SET
        cpf_hash = u.cpf_hash,
        cpf_cripto = u.cpf_cripto,
        cpf_last5 = u.cpf_last5
    FROM usuario u
        WHERE m.usuario_id = u.id;

ALTER table IF EXISTS reserva
    DROP CONSTRAINT IF EXISTS fkf1j5eiuw06tjtkv0m0tud30nl,
    DROP CONSTRAINT IF EXISTS reserva_motorista_id_fkey;

ALTER table IF EXISTS veiculo_empresa_motorista
    DROP CONSTRAINT IF EXISTS fk2gnw1312kl39tfaw54j9gi6xp,
    DROP CONSTRAINT IF EXISTS veiculo_empresa_motorista_motorista_id_fkey;

UPDATE reserva r
    SET motorista_id = m.usuario_id
        FROM motorista m
            WHERE r.motorista_id = m.id;

UPDATE veiculo_empresa_motorista vm
    SET motorista_id = m.usuario_id
        FROM motorista m
            WHERE vm.motorista_id = m.id;

UPDATE motorista
    SET id = usuario_id;

ALTER TABLE IF EXISTS motorista
    DROP CONSTRAINT IF EXISTS motorista_usuario_id_key;

ALTER TABLE IF EXISTS motorista
    DROP CONSTRAINT IF EXISTS motorista_id_fkey;

ALTER TABLE IF EXISTS motorista
    ADD CONSTRAINT motorista_id_fkey
        FOREIGN KEY (id)
            REFERENCES usuario(id);

ALTER TABLE IF EXISTS motorista
    DROP COLUMN IF EXISTS usuario_id;

ALTER TABLE IF EXISTS reserva
    DROP CONSTRAINT IF EXISTS fk_reserva_motorista;

ALTER TABLE IF EXISTS reserva
    ADD CONSTRAINT fk_reserva_motorista
        FOREIGN KEY (motorista_id)
            REFERENCES motorista(id);

ALTER TABLE IF EXISTS veiculo_empresa_motorista
    DROP CONSTRAINT IF EXISTS fk_vem_motorista;

ALTER TABLE veiculo_empresa_motorista
    ADD CONSTRAINT fk_vem_motorista
        FOREIGN KEY (motorista_id)
            REFERENCES motorista(id);


-- agente

ALTER TABLE IF EXISTS agente
    ADD COLUMN IF NOT EXISTS cpf_hash VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cpf_cripto VARCHAR(255),
    ADD COLUMN IF NOT EXISTS cpf_last5 VARCHAR(5);

UPDATE agente a
    SET
        cpf_hash = u.cpf_hash,
        cpf_cripto = u.cpf_cripto,
        cpf_last5 = u.cpf_last5
    FROM usuario u
        WHERE a.usuario_id = u.id;

ALTER table IF EXISTS reserva_rapida
    DROP CONSTRAINT IF EXISTS fkhmdwyhjpdqf6ijjtdg61m2o48,
    DROP CONSTRAINT IF EXISTS reserva_rapida_agente_id_fkey;

UPDATE reserva_rapida rr
    SET agente_id = a.usuario_id
        FROM agente a
            WHERE rr.agente_id = a.id;

UPDATE agente
    SET id = usuario_id;

ALTER TABLE IF EXISTS agente
    DROP CONSTRAINT IF EXISTS agente_usuario_id_key;

ALTER TABLE IF EXISTS agente
    DROP CONSTRAINT IF EXISTS agente_id_fkey;

ALTER TABLE IF EXISTS agente
    ADD CONSTRAINT agente_id_fkey
        FOREIGN KEY (id)
            REFERENCES usuario(id);

ALTER TABLE IF EXISTS agente
    DROP COLUMN IF EXISTS usuario_id;

ALTER TABLE IF EXISTS reserva_rapida
    DROP CONSTRAINT IF EXISTS fk_reserva_rapida_agente;

ALTER TABLE IF EXISTS reserva_rapida
    ADD CONSTRAINT fk_reserva_rapida_agente
        FOREIGN KEY (agente_id)
            REFERENCES agente(id);

--usuario
ALTER TABLE IF EXISTS usuario
    DROP COLUMN IF EXISTS cpf_hash,
    DROP COLUMN IF EXISTS cpf_cripto,
    DROP COLUMN IF EXISTS cpf_last5;