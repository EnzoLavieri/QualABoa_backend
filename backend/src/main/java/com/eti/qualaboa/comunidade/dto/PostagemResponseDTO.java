package com.eti.qualaboa.comunidade.dto;

import com.eti.qualaboa.comunidade.domain.entity.Postagem;
import com.eti.qualaboa.comunidade.domain.entity.Reacao;
import com.eti.qualaboa.comunidade.domain.enums.TipoPostagem;
import com.eti.qualaboa.comunidade.domain.enums.TipoReacao;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public record PostagemResponseDTO(
        Long id,
        String conteudoTexto,
        String mediaUrl,
        LocalDateTime dataHora,
        TipoPostagem tipo,
        List<OpcaoResponseDTO> opcoes,
        int totalReacoes,
        Map<TipoReacao, Long> reacoesDetalhadas
) {
    public PostagemResponseDTO(Postagem p) {
        this(
                p.getId(),
                p.getConteudoTexto(),
                p.getMediaUrl(),
                p.getDataHora(),
                p.getTipo(),
                p.getOpcoesEnquete().stream().map(OpcaoResponseDTO::new).collect(Collectors.toList()),
                p.getReacoes().size(),
                p.getReacoes().stream()
                        .collect(Collectors.groupingBy(Reacao::getTipo, Collectors.counting()))
        );
    }
}