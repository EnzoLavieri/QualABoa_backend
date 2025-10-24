package com.eti.qualaboa.estabelecimento.dto;

import com.eti.qualaboa.endereco.Endereco;
import com.eti.qualaboa.enums.Sexo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
public class EstabelecimentoResponseDTO {
    private Long idEstabelecimento;
    private String nome;
    private String email;
    private String categoria;
    private String descricao;
    private String telefone;
    private Endereco endereco;
    private Double classificacao;
    private List<String> conveniencias;

    public EstabelecimentoResponseDTO(Long id, String nome, String email,String categoria,String descricao,String telefone,Endereco endereco,Double classificacao,  List<String> conveniencias) {
        this.idEstabelecimento = id;
        this.nome = nome;
        this.email = email;
        this.categoria = categoria;
        this.descricao = descricao;
        this.telefone = telefone;
        this.endereco = endereco;
        this.classificacao = classificacao;
        this.conveniencias = conveniencias;
    }
}
