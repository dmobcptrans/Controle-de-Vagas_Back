CREATE TABLE IF NOT EXISTS gestor (
    id UUID PRIMARY KEY REFERENCES usuario(id),
    cpf_hash VARCHAR(255) NOT NULL,
    cpf_cripto VARCHAR(255) NOT NULL,
    cpf_last5 VARCHAR(5) NOT NULL
);


INSERT INTO gestor (id, cpf_hash, cpf_cripto, cpf_last5)
    SELECT
        u.id,
        u.cpf_hash,
        u.cpf_cripto,
        u.cpf_last5
        FROM usuario u
            WHERE u.permissao = 'GESTOR'
                AND NOT EXISTS (
                    SELECT 1
                    FROM gestor g
                    WHERE g.id = u.id
                );