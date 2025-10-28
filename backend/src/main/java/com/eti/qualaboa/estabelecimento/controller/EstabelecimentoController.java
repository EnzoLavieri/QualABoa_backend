package com.eti.qualaboa.estabelecimento.controller;

import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estabelecimentos")
@RequiredArgsConstructor
public class EstabelecimentoController {

    private final EstabelecimentoService service;

    @PostMapping
    public ResponseEntity<EstabelecimentoDTO> criar(@RequestBody Estabelecimento estabelecimento) {
        return ResponseEntity.ok(service.criar(estabelecimento));
    }

    @GetMapping
    public ResponseEntity<List<EstabelecimentoDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstabelecimentoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstabelecimentoDTO> atualizar(
            @PathVariable Long id,
            @RequestBody Estabelecimento estabelecimento
    ) {
        return ResponseEntity.ok(service.atualizar(id, estabelecimento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletar(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/link-place")
    public ResponseEntity<EstabelecimentoDTO> vincularPlace(
            @PathVariable Long id,
            @RequestParam String placeId
    ) {
        return ResponseEntity.ok(service.vincularComPlace(id, placeId));
    }
}
