package com.eti.qualaboa.promocao.model;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "promocoes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promocao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idPromocao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEstabelecimento", nullable = false)
    private Estabelecimento estabelecimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCupom")
    private Cupom cupom;

    private String descricao;
    private double desconto; //por exemplo: 10% de desconto em drinks
    private int totalVisualizacoes;
    private int totalCliques;
    private boolean ativa = true;
}

