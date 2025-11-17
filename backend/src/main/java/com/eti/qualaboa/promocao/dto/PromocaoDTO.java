package com.eti.qualaboa.promocao.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PromocaoDTO {
    private Long idPromocao;
    private Long idEstabelecimento;
    private Long idCupom;
    private String descricao;
    private double desconto;
    private int totalVisualizacoes;
    private int totalCliques;
    private boolean ativa;
}
