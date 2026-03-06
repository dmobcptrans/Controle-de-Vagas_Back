ALTER TABLE usuario
    ADD COLUMN aceitar_termos boolean default false not null;

ALTER TABLE usuario
    ADD COLUMN versao_termos varchar(10) default '1.0.0' not null;

ALTER TABLE usuario
    ADD COLUMN aceitou_termos_em timestamptz default null;