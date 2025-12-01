package com.eti.qualaboa.comunidade.domain.entity;

import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "opcoes_enquete")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OpcaoEnquete {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String textoOpcao;

    @ManyToOne
    @JoinColumn(name = "postagem_id")
    @JsonIgnore
    private Postagem postagem;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "votos_enquete",
            joinColumns = @JoinColumn(name = "opcao_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> votos = new HashSet<>();
}