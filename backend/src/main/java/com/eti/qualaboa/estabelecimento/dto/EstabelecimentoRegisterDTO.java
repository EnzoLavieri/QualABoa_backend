package com.eti.qualaboa.estabelecimento.dto;

import com.eti.qualaboa.endereco.Endereco;
import jakarta.persistence.Column;
import lombok.Data;

import java.util.List;

@Data
public class EstabelecimentoRegisterDTO {
    private Long idEstabelecimento;
    private String nome;
    private String email;
    private String senha;
    private String categoria;
    private String descricao;
    private String telefone;
    private Boolean parceiro;
    private String placeId;
    private Double latitude;
    private Double longitude;

    @Column(length = 1000)
    private String enderecoFormatado;

    private Endereco endereco;
    private Double classificacao;
    private List<String> conveniencias;
    private Long idRole;
}
