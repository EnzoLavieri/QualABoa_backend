package com.eti.qualaboa.cupom.dto;

import com.eti.qualaboa.enums.TipoCupom;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CupomDTO {
    private Long idCupom;
    private String codigo;
    private String descricao;
    private TipoCupom tipo;
    private Double valor;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private boolean ativo;
    private int quantidadeTotal;
    private int quantidadeUsada;
    private Long idEstabelecimento;
}

