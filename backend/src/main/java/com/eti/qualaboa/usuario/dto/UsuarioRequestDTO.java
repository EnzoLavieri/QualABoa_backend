package com.eti.qualaboa.usuario.dto;

import com.eti.qualaboa.enums.Sexo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class UsuarioRequestDTO {

    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @Email(message = "O formato do e-mail é inválido.")
    @NotBlank(message = "O e-mail é obrigatório.")
    private String email;

    @NotBlank(message = "A senha é obrigatória.")
    private String senha;

    @NotNull(message = "O sexo é obrigatório.")
    private Sexo sexo;

    private List<String> preferenciasUsuario;
}
