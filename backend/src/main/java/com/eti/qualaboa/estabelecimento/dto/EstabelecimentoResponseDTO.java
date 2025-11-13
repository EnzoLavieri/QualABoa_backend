package com.eti.qualaboa.estabelecimento.dto;

import com.eti.qualaboa.endereco.Endereco;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
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
    private Boolean parceiro;
    private String placeId;
    private Double latitude;
    private Double longitude;
    private String enderecoFormatado;
    private Endereco endereco;
    private Double classificacao;
    private List<String> conveniencias;
    private String fotoUrl;

    public EstabelecimentoResponseDTO(Long id, String nome, String email, String categoria,
                                      String descricao, String telefone, Boolean parceiro,
                                      String placeId, Double latitude, Double longitude,
                                      String enderecoFormatado, Endereco endereco, Double classificacao,
                                      List<String> conveniencias) {
        this.idEstabelecimento = id;
        this.nome = nome;
        this.email = email;
        this.categoria = categoria;
        this.descricao = descricao;
        this.telefone = telefone;
        this.endereco = endereco;
        this.classificacao = classificacao;
        this.conveniencias = conveniencias;
        this.fotoUrl = fotoUrl;
    }

    public EstabelecimentoResponseDTO(Estabelecimento estabelecimento) {
        this.idEstabelecimento = estabelecimento.getIdEstabelecimento();
        this.nome = estabelecimento.getNome();
        this.email = estabelecimento.getEmail();
        this.categoria = estabelecimento.getCategoria();
        this.descricao = estabelecimento.getDescricao();
        this.telefone = estabelecimento.getTelefone();
        this.parceiro = estabelecimento.getParceiro();
        this.placeId = estabelecimento.getPlaceId();
        this.latitude = estabelecimento.getLatitude();
        this.longitude = estabelecimento.getLongitude();
        this.enderecoFormatado = estabelecimento.getEnderecoFormatado();
        this.endereco = estabelecimento.getEndereco();
        this.classificacao = estabelecimento.getClassificacao();
        this.conveniencias = estabelecimento.getConveniencias();
        this.fotoUrl = estabelecimento.getFotoUrl();
    }
}
