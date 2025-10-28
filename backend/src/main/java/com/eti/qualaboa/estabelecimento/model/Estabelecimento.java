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

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "idEndereco")
    private Endereco endereco;

    @Lob
    @JsonIgnore
    private byte[] imagemPerfil;

    private Double classificacao;

    @ElementCollection
    @CollectionTable(
            name = "estabelecimento_conveniencias",
            joinColumns = @JoinColumn(name = "idEstabelecimento")
    )
    @Column(name = "conveniencia")
    private List<String> conveniencias;

    // -------- integração com Google Places / Maps ----------
    private Boolean parceiro = false;       // se é parceiro do app
    private String placeId;                 // ID do Google Places
    private Double latitude;
    private Double longitude;

    @Column(length = 1000)
    private String enderecoFormatado;       // endereço obtido pelo Places

    //relacionamentos
    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Evento> eventos;

    @OneToMany(mappedBy = "estabelecimento", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<Cupom> cupons;
}
