package com.eti.qualaboa.estabelecimento.dto;

import com.eti.qualaboa.endereco.Endereco;
import lombok.*;
import java.util.List;

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
    private List<String> conveniencias;
    private Boolean parceiro;
    private String placeId;
    private Double latitude;
    private Double longitude;
    private String enderecoFormatado;
    private String fotoUrl;
}
