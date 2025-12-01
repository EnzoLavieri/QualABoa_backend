package com.eti.qualaboa.comunidade.domain.entity;

import com.eti.qualaboa.comunidade.domain.enums.TipoReacao;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "reacoes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "postagem_id")
    private Postagem postagem;

    @Enumerated(EnumType.STRING)
    private TipoReacao tipo;
}