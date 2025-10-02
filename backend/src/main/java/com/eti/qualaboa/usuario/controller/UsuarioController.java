package com.eti.qualaboa.usuario.controller;

import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.dto.UsuarioRequestDTO;
import com.eti.qualaboa.usuario.dto.UsuarioResponseDTO;
import com.eti.qualaboa.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping()
    public ResponseEntity<UsuarioResponseDTO> criarUsuario(@RequestBody @Valid UsuarioRequestDTO requestDTO){
        UsuarioResponseDTO userResponseDTO = usuarioService.criarUsuario(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }

    @GetMapping("/{id}")
    public  ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id){
        Usuario user = usuarioService.findUserById(id);

        UsuarioResponseDTO userResponseDTO = new UsuarioResponseDTO(user.getId(), user.getNome(), user.getEmail(),user.getSexo(),user.getPreferenciasUsuario());

        return ResponseEntity.ok(userResponseDTO);
    }

}
