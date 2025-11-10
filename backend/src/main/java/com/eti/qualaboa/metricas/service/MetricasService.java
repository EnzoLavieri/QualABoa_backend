package com.eti.qualaboa.metricas.service;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.metricas.model.Metricas;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricasService {

    private final EstabelecimentoRepository estabelecimentoRepository;

    @Transactional
    public void registrarClique(Long id) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));
        Metricas metricas = estabelecimento.getMetricas();
        metricas.setCliques(metricas.getCliques() + 1);
        estabelecimentoRepository.save(estabelecimento);
    }

    @Transactional
    public void registrarFavorito(Long id) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));
        Metricas metricas = estabelecimento.getMetricas();
        metricas.setTotalFavoritos(metricas.getTotalFavoritos() + 1);
        estabelecimentoRepository.save(estabelecimento);
    }


}
