package com.eti.qualaboa.comunidade.dto;

import com.eti.qualaboa.comunidade.domain.entity.OpcaoEnquete;

public record OpcaoResponseDTO(Long id, String texto, int votos) {
    public OpcaoResponseDTO(OpcaoEnquete op) {
        this(op.getId(), op.getTextoOpcao(), op.getVotos().size());
    }
}