package com.eti.qualaboa.comunidade.domain.entity;

import com.eti.qualaboa.comunidade.domain.enums.TipoPostagem;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "postagens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Postagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String conteudoTexto;

    @Column(length = 1000)
    private String mediaUrl;

    private LocalDateTime dataHora;

    @Enumerated(EnumType.STRING)
    private TipoPostagem tipo;

    @ManyToOne
    @JoinColumn(name = "comunidade_id", nullable = false)
    private Comunidade comunidade;

    @OneToMany(mappedBy = "postagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OpcaoEnquete> opcoesEnquete = new ArrayList<>();

    @OneToMany(mappedBy = "postagem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reacao> reacoes = new ArrayList<>();
}