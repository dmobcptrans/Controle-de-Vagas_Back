DELETE FROM push_token
WHERE id NOT IN (
    SELECT DISTINCT ON (token) id
FROM push_token
ORDER BY token, criado_em DESC
    );

ALTER TABLE push_token
    ADD CONSTRAINT uk_push_token_token UNIQUE (token);