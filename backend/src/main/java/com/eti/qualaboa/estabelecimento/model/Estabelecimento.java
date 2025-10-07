package com.eti.qualaboa.estabelecimento.model;

import com.eti.qualaboa.endereco.Endereco;
import com.eti.qualaboa.evento.model.Evento;
import com.eti.qualaboa.cupom.model.Cupom;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "estabelecimentos")
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
    private String categoria;
    private String descricao;
    private String telefone;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "id_endereco")
    @JsonManagedReference
    private Endereco endereco;

    @Lob
    @JsonIgnore
    private byte[] imagemPerfil;

    private Double classificacao;
    private String conveniencias;

    //Relacionamentos com Evento e Cupom
    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evento> eventos;

    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cupom> cupons;
}


