CREATE TABLE IF NOT EXISTS convite_motorista_empresa (
    id uuid PRIMARY KEY,
    motorista_id uuid REFERENCES motorista (id),
    motorista_email_hash VARCHAR(255) NOT NULL,
    motorista_email_cripto VARCHAR(255) NOT NULL,
    empresa_id uuid NOT NULL REFERENCES empresa (id),
    status VARCHAR(30) NOT NULL,
    token_hash VARCHAR(255) UNIQUE,
    criado_em TIMESTAMPTZ NOT NULL,
    respondido_em TIMESTAMPTZ,
    valido_ate TIMESTAMPTZ NOT NULL,
    cripto_version INT NOT NULL
);