DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_name = 'usuario'
          AND column_name = 'personal_data_key_version'
    ) THEN
        ALTER TABLE usuario
            RENAME COLUMN personal_data_key_version TO cripto_version;
    END IF;
END $$;

ALTER TABLE IF EXISTS usuario
   ADD COLUMN IF NOT EXISTS hash_version INT DEFAULT 1;