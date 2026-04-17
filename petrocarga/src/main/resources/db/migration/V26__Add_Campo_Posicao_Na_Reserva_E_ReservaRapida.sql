ALTER TABLE reserva
ADD COLUMN posicao_perpendicular INTEGER;

ALTER TABLE reserva_rapida
ADD COLUMN posicao_perpendicular INTEGER;

ALTER TABLE reserva
ADD CONSTRAINT ck_reserva_posicao_positiva
CHECK (posicao_perpendicular IS NULL OR posicao_perpendicular >= 1);

ALTER TABLE reserva_rapida
ADD CONSTRAINT ck_reserva_rapida_posicao_positiva
CHECK (posicao_perpendicular IS NULL OR posicao_perpendicular >= 1);

ALTER TABLE reserva_rapida
ADD COLUMN cidade_origem VARCHAR(255),
ADD COLUMN entrada_cidade VARCHAR(255);