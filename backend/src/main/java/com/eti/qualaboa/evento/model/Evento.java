package com.eti.qualaboa.evento.model;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.cupom.model.Cupom;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "eventos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idEstabelecimento", nullable = false)
    private Estabelecimento estabelecimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "idCupom")
    private Cupom cupom;

    private String descricao;
    private String titulo;
    private String data;
    private String horario;
    private int totalVisualizacoes;
    private int totalCliques;
}
