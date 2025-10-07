package com.eti.qualaboa.cupom.controller;

import com.eti.qualaboa.cupom.dto.CupomDTO;
import com.eti.qualaboa.cupom.service.CupomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cupons")
@RequiredArgsConstructor
public class CupomController {

    private final CupomService cupomService;

    @GetMapping
    public ResponseEntity<List<CupomDTO>> listar() {
        return ResponseEntity.ok(cupomService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CupomDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cupomService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<CupomDTO> criar(@RequestBody CupomDTO dto) {
        return ResponseEntity.ok(cupomService.criarCupom(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CupomDTO> atualizar(@PathVariable Long id, @RequestBody CupomDTO dto) {
        return ResponseEntity.ok(cupomService.atualizarCupom(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        cupomService.deletarCupom(id);
        return ResponseEntity.noContent().build();
    }
}

