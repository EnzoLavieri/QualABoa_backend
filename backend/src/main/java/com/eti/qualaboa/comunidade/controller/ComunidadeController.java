package com.eti.qualaboa.comunidade.controller;

import com.eti.qualaboa.comunidade.domain.enums.TipoReacao;
import com.eti.qualaboa.comunidade.dto.CriarPostagemDTO;
import com.eti.qualaboa.comunidade.dto.PostagemResponseDTO;
import com.eti.qualaboa.comunidade.service.ComunidadeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comunidades")
@RequiredArgsConstructor
public class ComunidadeController {

    private final ComunidadeService comunidadeService;

    @PostMapping("/{id}/entrar")
    public ResponseEntity<Void> entrar(@PathVariable Long id, @RequestParam Long usuarioId) {
        comunidadeService.entrarComunidade(id, usuarioId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/entrar/estabelecimento/{estabId}")
    public ResponseEntity<Void> entrarPorEstabelecimento(@PathVariable Long estabId, @RequestParam Long usuarioId) {
        comunidadeService.entrarComunidadePorEstabelecimento(estabId, usuarioId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/postagens")
    public ResponseEntity<PostagemResponseDTO> criarPostagem(
            @PathVariable Long id,
            @RequestParam Long estabelecimentoId,
            @RequestBody CriarPostagemDTO dto) {
        return ResponseEntity.ok(comunidadeService.criarPostagem(id, estabelecimentoId, dto));
    }

    @GetMapping("/{id}/postagens")
    public ResponseEntity<Page<PostagemResponseDTO>> listarPostagens(
            @PathVariable Long id,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(comunidadeService.listarPostagens(id, pageable));
    }

    @PostMapping("/postagens/{postId}/reagir")
    public ResponseEntity<Void> reagir(
            @PathVariable Long postId,
            @RequestParam Long usuarioId,
            @RequestParam TipoReacao tipo) {
        comunidadeService.reagir(postId, usuarioId, tipo);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/enquete/{opcaoId}/votar")
    public ResponseEntity<Void> votar(
            @PathVariable Long opcaoId,
            @RequestParam Long usuarioId) {
        comunidadeService.votarEnquete(opcaoId, usuarioId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<java.util.List<com.eti.qualaboa.comunidade.dto.ComunidadeResponseDTO>> listarMinhasComunidades(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(comunidadeService.listarMinhasComunidades(usuarioId));
    }
}