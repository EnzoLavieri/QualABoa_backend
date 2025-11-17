package com.eti.qualaboa.usuario.dto;

import com.eti.qualaboa.usuario.domain.entity.Usuario;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsuarioUpdateResponseDTO {

    private String nome;
    private List<String> preferenciasUsuario;
    private String fotoUrl;

    public UsuarioUpdateResponseDTO(Usuario user) {
        this.nome = user.getNome();
        this.preferenciasUsuario = user.getPreferenciasUsuario();
        this.fotoUrl = user.getFotoUrl();
    }
}
