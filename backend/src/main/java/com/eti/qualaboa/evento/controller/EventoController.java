package com.eti.qualaboa.evento.controller;

import com.eti.qualaboa.evento.dto.EventoDTO;
import com.eti.qualaboa.evento.service.EventoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/eventos")
@RequiredArgsConstructor
public class EventoController {

    private final EventoService eventoService;

    @GetMapping
    public ResponseEntity<List<EventoDTO>> listar() {
        return ResponseEntity.ok(eventoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventoDTO> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(eventoService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<EventoDTO> criar(@RequestBody EventoDTO dto) {
        return ResponseEntity.ok(eventoService.criarEvento(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventoDTO> atualizar(@PathVariable Long id, @RequestBody EventoDTO dto) {
        return ResponseEntity.ok(eventoService.atualizarEvento(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        eventoService.deletarEvento(id);
        return ResponseEntity.noContent().build();
    }
}

