-- Login passa a ser o e-mail do usuário.
-- Normaliza registros antigos e converte o administrador padrão para e-mail.
UPDATE usuarios
SET login = lower(trim(login))
WHERE login <> lower(trim(login));

UPDATE usuarios
SET login = 'admin@controleobras.com'
WHERE login = 'admin'
  AND NOT EXISTS (SELECT 1 FROM usuarios WHERE login = 'admin@controleobras.com');
