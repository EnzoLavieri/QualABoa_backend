package com.eti.qualaboa.usuario.controller;

import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.dto.*;
import com.eti.qualaboa.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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
    @PreAuthorize("hasAuthority('SCOPE_ADMIN')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id){
        Usuario user = usuarioService.findUserById(id);

        UsuarioResponseDTO userResponseDTO = new UsuarioResponseDTO(user.getId(), user.getNome(), user.getEmail(),user.getSexo(),user.getPreferenciasUsuario());

        return ResponseEntity.ok(userResponseDTO);
    }

    @PostMapping("/favoritar/{estabelecimentoId}")
    public ResponseEntity<Void> adicionarFavorito(@PathVariable Long estabelecimentoId, @RequestBody FavoritoRequestDTO requestDTO) {
        Usuario user = usuarioService.findUserById(requestDTO.getUserId());
        usuarioService.favoritarEstabelecimento(user.getId(), estabelecimentoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/excluirFavorito/{estabelecimentoId}")
    public ResponseEntity<Void> excluirFavorito(@PathVariable Long estabelecimentoId, @RequestBody FavoritoRequestDTO requestDTO) {
        Usuario user = usuarioService.findUserById(requestDTO.getUserId());
        usuarioService.excluirFavoritos(user.getId(), estabelecimentoId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioUpdateResponseDTO> atualizarUsuario(@PathVariable Long id, @RequestBody @Valid UsuarioUpdateRequestDTO requestDTO){
        UsuarioUpdateResponseDTO userResponseDTO = usuarioService.atualizarUsuario(id,requestDTO);

        return ResponseEntity.status(HttpStatus.OK).body(userResponseDTO);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<HttpStatus> deletarUsuario(@PathVariable Long id){
        HttpStatus user = usuarioService.deletarUsuario(id);
        return  ResponseEntity.noContent().build();
    }

}
