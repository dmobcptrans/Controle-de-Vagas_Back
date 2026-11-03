BEGIN;

-- 1. Adicionar codigo_pmp à tabela vaga
ALTER TABLE vaga
ADD COLUMN codigo_pmp VARCHAR(6);

-- 2. Gerar códigos temporários únicos para todas as vagas
WITH codigos AS (
    SELECT
        id,
        'T' || LPAD(
            ROW_NUMBER() OVER (ORDER BY id)::TEXT,
            5,
            '0'
        ) AS novo_codigo
    FROM vaga
)
UPDATE vaga v
SET codigo_pmp = c.novo_codigo
FROM codigos c
WHERE v.id = c.id;

-- 3. Garantir que todas as vagas receberam um código
DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM vaga
        WHERE codigo_pmp IS NULL
    ) THEN
        RAISE EXCEPTION
            'Existem vagas sem codigo_pmp.';
    END IF;

    IF EXISTS (
        SELECT 1
        FROM vaga
        GROUP BY codigo_pmp
        HAVING COUNT(*) > 1
    ) THEN
        RAISE EXCEPTION
            'Existem códigos PMP duplicados.';
    END IF;
END $$;

-- 4. Tornar o campo obrigatório
ALTER TABLE vaga
ALTER COLUMN codigo_pmp SET NOT NULL;

-- 5. Criar restrição de unicidade
ALTER TABLE vaga
ADD CONSTRAINT uk_vaga_codigo_pmp UNIQUE (codigo_pmp);

-- 6. Remover codigo_pmp de endereco_vaga
ALTER TABLE endereco_vaga
DROP COLUMN codigo_pmp;

COMMIT;