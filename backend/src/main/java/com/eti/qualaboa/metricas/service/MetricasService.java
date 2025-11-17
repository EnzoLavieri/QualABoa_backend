package com.eti.qualaboa.metricas.service;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import com.eti.qualaboa.metricas.model.LogBuscaPeloNome;
import com.eti.qualaboa.metricas.model.LogClique;
import com.eti.qualaboa.metricas.model.LogFavoritos;
import com.eti.qualaboa.metricas.model.Metricas;
import com.eti.qualaboa.metricas.repository.LogBuscaPeloNomeRepository;
import com.eti.qualaboa.metricas.repository.LogCliqueRepository;
import com.eti.qualaboa.metricas.repository.LogFavoritosRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MetricasService {

    private final EstabelecimentoRepository estabelecimentoRepository;
    private final LogCliqueRepository logCliqueRepository;
    private final LogFavoritosRepository logFavoritosRepository;
    private final LogBuscaPeloNomeRepository logBuscaPeloNomeRepository;

    @Transactional
    public void registrarClique(Long id) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));
        Metricas metricas = estabelecimento.getMetricas();
        metricas.setCliques(metricas.getCliques() + 1);
        estabelecimentoRepository.save(estabelecimento);

        LogClique novoClique = new LogClique();
        novoClique.setEstabelecimento(estabelecimento);
        novoClique.setDataHoraClique(LocalDateTime.now());
        logCliqueRepository.save(novoClique);
    }

    @Transactional
    public void registrarFavorito(Long id) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));
        Metricas metricas = estabelecimento.getMetricas();
        metricas.setTotalFavoritos(metricas.getTotalFavoritos() + 1);
        estabelecimentoRepository.save(estabelecimento);

        LogFavoritos novoFavorito = new LogFavoritos();
        novoFavorito.setEstabelecimento(estabelecimento);
        novoFavorito.setDataHoraFavorito(LocalDateTime.now());
        logFavoritosRepository.save(novoFavorito);
    }

    @Transactional
    public void registrarBuscaPeloNome(String nome) {
        String nomeBusca = normalizarNome(nome);
        Estabelecimento estabelecimento = estabelecimentoRepository.findByNomeNormalizado(nomeBusca)
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        Boolean nomeEncontrado = estabelecimento.getNomeNormalizado().equals(nomeBusca);

        if (nomeEncontrado) {
            LogBuscaPeloNome buscaPeloNome = new LogBuscaPeloNome();
            buscaPeloNome.setEstabelecimento(estabelecimento);
            buscaPeloNome.setDataHoraBusca(LocalDateTime.now());
            logBuscaPeloNomeRepository.save(buscaPeloNome);
        }

    }

    public String normalizarNome(String nome) {
        if (nome == null) {
            return null;
        }
        String nomeSemAcento = Normalizer.normalize(nome, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return nomeSemAcento.toUpperCase().trim();
    }
}
