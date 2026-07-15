-- Remove a foreign key (caso exista)
ALTER TABLE IF EXISTS disponibilidade_vaga
    DROP CONSTRAINT IF EXISTS fkgbupf5e87mfki7k5dpo91s1b4,
    DROP CONSTRAINT IF EXISTS disponibilidade_vaga_criado_por_fkey;

-- Renomeia a coluna
ALTER TABLE IF EXISTS disponibilidade_vaga
    RENAME COLUMN criado_por TO criado_por_id;