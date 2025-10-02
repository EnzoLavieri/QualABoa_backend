package com.eti.qualaboa.estabelecimento.model;

import com.eti.qualaboa.endereco.Endereco;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "Estabelecimento")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstabelecimento;

    private String nome;
    private String senha;
    private String email;
    private String categoria;//bar, restaurante, etc
    private String descricao;
    private String telefone;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idEndereco")
    @JsonManagedReference
    private Endereco endereco;


    @Lob
    @JsonIgnore
    private byte[] imagemPerfil;//tentar resolver problema

    private Double classificacao;
    private String conveniencias;//wifi,ar condicionado, etc
}

