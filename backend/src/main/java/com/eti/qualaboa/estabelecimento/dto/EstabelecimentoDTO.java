package com.eti.qualaboa.estabelecimento.dto;

import com.eti.qualaboa.endereco.Endereco;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstabelecimentoDTO {
    private Long idEstabelecimento;
    private String nome;
    private String email;
    private String categoria;
    private String descricao;
    private String telefone;
    private Endereco endereco;
    private Double classificacao;
    private String conveniencias;
}

