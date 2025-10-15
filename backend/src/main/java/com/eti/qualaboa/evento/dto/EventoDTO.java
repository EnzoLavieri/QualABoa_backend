package com.eti.qualaboa.evento.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoDTO {
    private Long idEvento;
    private Long idEstabelecimento;
    private Long idCupom;
    private String descricao;
    private String titulo;
    private String data;
    private String horario;
    private int totalVisualizacoes;
    private int totalCliques;
}

