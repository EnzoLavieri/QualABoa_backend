package com.eti.qualaboa.estabelecimento.repository;

import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoRegisterDTO;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import com.eti.qualaboa.usuario.repository.RoleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EstabelecimentoService estabelecimentoService;

    @Autowired
    private ResourceLoader resourceLoader;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EstabelecimentoRepository estabelecimentoRepository;

    @Override
    public void run(String... args) throws Exception {

        if (estabelecimentoRepository.count() > 0) {
            return;
        }

        try {
            Resource resource = resourceLoader.getResource("classpath:estabelecimentos.json");
            InputStream inputStream = resource.getInputStream();

            List<EstabelecimentoRegisterDTO> dtos = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<EstabelecimentoRegisterDTO>>() {}
            );

            for (EstabelecimentoRegisterDTO dto : dtos) {
                try {
                    estabelecimentoService.criar(dto);
                    logger.info("Estabelecimento '{}' criado com sucesso.", dto.getNome());
                } catch (RuntimeException e) {
                    logger.warn("Falha ao criar estabelecimento '{}': {}", dto.getNome(), e.getMessage());
                }
            }
            logger.info("Carga de estabelecimentos concluída.");

        } catch (Exception e) {
            logger.error("Falha ao carregar o arquivo estabelecimentos.json", e);
        }
    }
}
