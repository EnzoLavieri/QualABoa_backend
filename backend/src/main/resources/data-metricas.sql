-- Popula 'log_cliques' para o Bar do Bird (ID 1) em várias datas
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-07 14:30:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-08 18:15:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-08 11:05:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-08 19:45:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-09 10:00:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-09 16:20:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-10 14:30:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-11 18:15:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-12 11:05:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-12 19:45:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-13 10:00:00', 1);
INSERT INTO log_cliques (dataHoraClique, estabelecimento_id) VALUES ('2025-11-14 16:20:00', 1);

-- Popula 'log_favoritos' para o Bar do Bird (ID 1) em várias datas
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-10 15:00:00', 1);
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-10 19:00:00', 1);
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-11 09:30:00', 1);
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-11 15:00:00', 1);
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-12 19:00:00', 1);
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-12 09:30:00', 1);
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-12 15:00:00', 1);
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-13 19:00:00', 1);
INSERT INTO log_favoritos (dataHoraFavorito, estabelecimento_id) VALUES ('2025-11-13 09:30:00', 1);

-- Popula 'log_busca_pelo_nome' para o Bar do Bird (ID 1) em várias datas
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-05 14:29:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-06 14:29:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-07 14:29:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-08 14:29:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-09 14:29:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-11 14:29:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-12 14:29:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-13 11:04:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-14 09:59:00', 1);
INSERT INTO log_busca_pelo_nome (dataHoraBusca, estabelecimento_id) VALUES ('2025-11-15 11:15:00', 1);


-- Atualiza a tabela 'metricas' com os totais agregados para o Bar do Bird (ID 1)
-- Total de 6 cliques e 3 favoritos
UPDATE metricas SET cliques = 12, totalFavoritos = 9 WHERE id = 1;