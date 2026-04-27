

ALTER TABLE vaga
    ADD COLUMN latitude_inicio DOUBLE PRECISION,
ADD COLUMN longitude_inicio DOUBLE PRECISION,
ADD COLUMN latitude_fim DOUBLE PRECISION,
ADD COLUMN longitude_fim DOUBLE PRECISION;


CREATE INDEX idx_vaga_lat_lng
    ON vaga(latitude_inicio, longitude_inicio);