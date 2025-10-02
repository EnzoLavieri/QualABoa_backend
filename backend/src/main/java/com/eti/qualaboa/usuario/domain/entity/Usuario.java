package com.eti.qualaboa.usuario.domain.entity;

import com.eti.qualaboa.enums.Sexo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@ToString
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nome;
    private String email;
    private String senha;
    private Sexo sexo;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name="usuario_preferencias", joinColumns=@JoinColumn(name="usuario_id"))
    private List<String> preferenciasUsuario;
    //private ArrayList<Estabelecimento> estabelecimentosFavoritos;
    // falta checkins e avaliações
    // falta imagens
}
