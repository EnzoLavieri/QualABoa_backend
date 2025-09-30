package com.eti.qualaboa.estabelecimento.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estabelecimentos")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Estabelecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idEstabelecimento;

    private String nome;
    private String email;
    private String senha;
    private String endereco;
    private String categoria;
    private String descricao;
    private String imagemPerfil;//url da imagem
    private String telefone;
}
