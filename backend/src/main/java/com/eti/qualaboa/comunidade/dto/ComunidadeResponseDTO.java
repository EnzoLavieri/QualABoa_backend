package com.eti.qualaboa.comunidade.dto;

import com.eti.qualaboa.comunidade.domain.entity.Comunidade;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ComunidadeResponseDTO(
        Long id,
        String nome,
        String descricao,
        String capaUrl,
        int totalMembros
) {
    public ComunidadeResponseDTO(Comunidade c) {
        this(
                c.getId(),
                c.getNome(),
                c.getDescricao(),
                c.getCapaUrl(),
                c.getMembros() != null ? c.getMembros().size() : 0
        );
    }
}