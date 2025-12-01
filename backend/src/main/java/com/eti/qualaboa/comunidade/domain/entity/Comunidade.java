package com.eti.qualaboa.comunidade.domain.entity;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "comunidades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comunidade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;

    @Column(length = 500)
    private String descricao;

    @Column(length = 1000)
    private String capaUrl;

    @OneToOne
    @JoinColumn(name = "estabelecimento_id", nullable = false, unique = true)
    private Estabelecimento dono;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "comunidade_membros",
            joinColumns = @JoinColumn(name = "comunidade_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> membros = new HashSet<>();

    @OneToMany(mappedBy = "comunidade", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Postagem> postagens;
}