INSERT INTO tb_role (role_id, nome) VALUES (1, 'ADMIN')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO tb_role (role_id, nome) VALUES (2, 'USER')
ON CONFLICT (role_id) DO NOTHING;

INSERT INTO tb_role (role_id, nome) VALUES (3, 'ESTABELECIMENTO')
    ON CONFLICT (role_id) DO NOTHING;