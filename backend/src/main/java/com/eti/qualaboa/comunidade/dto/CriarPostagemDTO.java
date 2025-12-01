package com.eti.qualaboa.comunidade.dto;

import com.eti.qualaboa.comunidade.domain.enums.TipoPostagem;
import java.util.List;

public record CriarPostagemDTO(
        String conteudoTexto,
        String mediaUrl,
        TipoPostagem tipo,
        List<String> opcoesEnquete
) {}