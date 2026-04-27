UPDATE vaga
SET 
    latitude_inicio = CAST(trim(split_part(referencia_geo_inicio, ',', 1)) AS DOUBLE PRECISION),
    longitude_inicio = CAST(trim(split_part(referencia_geo_inicio, ',', 2)) AS DOUBLE PRECISION)
WHERE referencia_geo_inicio IS NOT NULL;

UPDATE vaga
SET 
    latitude_fim = CAST(trim(split_part(referencia_geo_fim, ',', 1)) AS DOUBLE PRECISION),
    longitude_fim = CAST(trim(split_part(referencia_geo_fim, ',', 2)) AS DOUBLE PRECISION)
WHERE referencia_geo_fim IS NOT NULL;

ALTER TABLE vaga
    DROP  CONSTRAINT IF EXISTS uq_vaga_coordenadas;

ALTER TABLE vaga
    ADD CONSTRAINT uq_vaga_coordenadas
    UNIQUE (latitude_inicio, longitude_inicio, latitude_fim, longitude_fim);

ALTER TABLE vaga
    DROP CONSTRAINT IF EXISTS chk_latitude_inicio,
    DROP CONSTRAINT IF EXISTS chk_latitude_fim,
    DROP CONSTRAINT IF EXISTS chk_longitude_inicio,
    DROP CONSTRAINT IF EXISTS chk_longitude_fim;

ALTER TABLE vaga
    ADD CONSTRAINT chk_latitude_inicio CHECK (latitude_inicio BETWEEN -90 AND 90),
    ADD CONSTRAINT chk_latitude_fim CHECK (latitude_fim BETWEEN -90 AND 90),
    ADD CONSTRAINT chk_longitude_inicio CHECK (longitude_inicio BETWEEN -180 AND 180),
    ADD CONSTRAINT chk_longitude_fim CHECK (longitude_fim BETWEEN -180 AND 180);

ALTER TABLE vaga
    DROP COLUMN referencia_geo_inicio,
    DROP COLUMN referencia_geo_fim;