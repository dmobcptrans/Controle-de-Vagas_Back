DELETE FROM push_token;

ALTER TABLE push_token
	DROP CONSTRAINT IF EXISTS uk_push_token_token,
	DROP CONSTRAINT IF EXISTS uq_push_token_usuario,
	DROP CONSTRAINT IF EXISTS uq_usuario_token;

DROP INDEX IF EXISTS uq_usuario_token;

ALTER TABLE push_token 
	ADD CONSTRAINT uq_usuario_token UNIQUE (usuario_id, token);