package com.eti.qualaboa.usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UsuarioUpdateRequestDTO {
    @NotBlank(message = "Preencha o nome")
    private String nome;

    private List<String> preferenciasUsuario;
    private String fotoUrl;

}
