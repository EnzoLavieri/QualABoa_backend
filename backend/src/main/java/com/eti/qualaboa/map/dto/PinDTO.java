package com.eti.qualaboa.map.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PinDTO {
    private Long id;           // id do estabelecimento (se parceiro) ou null
    private String placeId;    // placeId (Google) se disponível
    private String nome;
    private Double lat;
    private Double lng;
    private Boolean isPartner;
    private String snippet;    // descrição curta
    private String endereco;
}
