ALTER TABLE IF EXISTS veiculo_empresa_motorista
ADD COLUMN IF NOT EXISTS empresa_id UUID;

UPDATE veiculo_empresa_motorista vem
    SET empresa_id = m.empresa_id
        FROM motorista m
        WHERE m.id = vem.motorista_id;

ALTER TABLE IF EXISTS veiculo_empresa_motorista
    ALTER COLUMN empresa_id SET NOT NULL,
    DROP CONSTRAINT IF EXISTS fk_vem_motorista_empresa;

ALTER TABLE IF EXISTS motorista
    DROP CONSTRAINT IF EXISTS uk_motorista_id_empresa,
    ADD CONSTRAINT  uk_motorista_id_empresa 
        UNIQUE (id, empresa_id);

ALTER TABLE IF EXISTS veiculo_empresa_motorista
    ADD CONSTRAINT fk_vem_motorista_empresa
        FOREIGN KEY (motorista_id, empresa_id)
            REFERENCES motorista (id, empresa_id);