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
    private String fotoUrl;

    public UsuarioResponseDTO(Long id, String nome, String email, Sexo sexo,
                              List<String> preferenciasUsuario, String fotoUrl) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.sexo = sexo;
        this.preferenciasUsuario = preferenciasUsuario;
        this.fotoUrl = fotoUrl;
    }

    public UsuarioResponseDTO(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.preferenciasUsuario = usuario.getPreferenciasUsuario();
        this.fotoUrl = usuario.getFotoUrl();
    }
}
