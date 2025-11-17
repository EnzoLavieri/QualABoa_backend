package com.eti.qualaboa.cupom.model;

import com.eti.qualaboa.enums.TipoCupom;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "cupons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cupom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idCupom;

    private String codigo;
    private String descricao;

    @Enumerated(EnumType.STRING)
    private TipoCupom tipo; //DESCONTO, CASHBACK, etc.

    private Double valor;
    private LocalDateTime dataInicio;
    private LocalDateTime dataFim;
    private boolean ativo = true;
    private int quantidadeTotal;
    private int quantidadeUsada = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEstabelecimento", nullable = false)
    private Estabelecimento estabelecimento;
}
