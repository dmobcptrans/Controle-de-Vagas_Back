ALTER TABLE vaga
   DROP CONSTRAINT IF EXISTS vaga_comprimento_check;

ALTER TABLE vaga
   ADD constraint vaga_comprimento_check check  (comprimento >= 5 AND comprimento <= 140);