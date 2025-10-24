package com.eti.qualaboa.estabelecimento.controller;

import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoRegisterDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoResponseDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/estabelecimento")
public class EstabelecimentoController {

    private final EstabelecimentoService serviceEstabelecimento;

    public EstabelecimentoController(EstabelecimentoService serviceEstabelecimento) {
        this.serviceEstabelecimento = serviceEstabelecimento;
    }

    @PostMapping
    public ResponseEntity<EstabelecimentoResponseDTO> criar(@RequestBody EstabelecimentoRegisterDTO estabelecimento) {
        EstabelecimentoResponseDTO salvo = serviceEstabelecimento.criar(estabelecimento);
        return ResponseEntity.created(URI.create("/estabelecimento/" + salvo.getIdEstabelecimento())).body(salvo);
    }

    @GetMapping
    public ResponseEntity<List<EstabelecimentoDTO>> listar() {
        return ResponseEntity.ok(serviceEstabelecimento.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstabelecimentoDTO> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(serviceEstabelecimento.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstabelecimentoDTO> atualizar(@PathVariable Long id, @RequestBody Estabelecimento estabelecimento) {
        return ResponseEntity.ok(serviceEstabelecimento.atualizar(id, estabelecimento));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        serviceEstabelecimento.deletar(id);
        return ResponseEntity.noContent().build();
    }
}
