ALTER TABLE IF EXISTS operacao_vaga 
	DROP CONSTRAINT IF exists operacao_vaga_vaga_id_dia_semana_key,
	DROP CONSTRAINT IF exists uq_operacao_vaga_id_vaga_id_dia_semana_hora;

ALTER TABLE IF EXISTS operacao_vaga 
	ADD CONSTRAINT uq_operacao_vaga_id_vaga_id_dia_semana_hora 
        UNIQUE(id, vaga_id, dia_semana, hora_inicio, hora_fim);