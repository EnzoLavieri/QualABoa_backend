package com.eti.qualaboa.promocao.controller;

import com.eti.qualaboa.promocao.dto.PromocaoDTO;
import com.eti.qualaboa.promocao.service.PromocaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/promocoes")
@RequiredArgsConstructor
public class PromocaoController {

    private final PromocaoService promocaoService;

    @GetMapping
    public List<PromocaoDTO> listarTodas() {
        return promocaoService.listarTodas();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PromocaoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(promocaoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<PromocaoDTO> criar(@RequestBody PromocaoDTO dto) {
        return ResponseEntity.ok(promocaoService.criarPromocao(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PromocaoDTO> atualizar(@PathVariable Long id, @RequestBody PromocaoDTO dto) {
        return ResponseEntity.ok(promocaoService.atualizarPromocao(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        promocaoService.deletarPromocao(id);
        return ResponseEntity.noContent().build();
    }
}
