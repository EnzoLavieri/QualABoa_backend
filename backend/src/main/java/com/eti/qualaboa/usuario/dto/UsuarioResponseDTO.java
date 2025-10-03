package com.eti.qualaboa.usuario.dto;

import com.eti.qualaboa.enums.Sexo;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsuarioResponseDTO {
    private Long id;
    private String nome;
    private String email;
    private Sexo sexo;
    private List<String> preferenciasUsuario;

    public UsuarioResponseDTO(Long id, String nome, String email, Sexo sexo, List<String> preferenciasUsuario) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.sexo = sexo;
        this.preferenciasUsuario = preferenciasUsuario;
    }
}
