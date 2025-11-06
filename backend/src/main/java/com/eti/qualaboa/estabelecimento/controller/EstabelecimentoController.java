package com.eti.qualaboa.estabelecimento.controller;

import com.eti.qualaboa.cupom.dto.CupomDTO;
import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoRegisterDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoResponseDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/estabelecimentos")
@RequiredArgsConstructor
@Slf4j
public class EstabelecimentoController {

    private final EstabelecimentoService service;

    @PostMapping
    public ResponseEntity<EstabelecimentoResponseDTO> criar(@RequestBody EstabelecimentoRegisterDTO estabelecimento) {
        log.info("Recebido EstabelecimentoRegisterDTO para criação: {}", estabelecimento);
        return ResponseEntity.ok(service.criar(estabelecimento));
    }

    // public ResponseEntity<EstabelecimentoResponseDTO> criar(@RequestBody
    // EstabelecimentoRegisterDTO estabelecimento) {
    // EstabelecimentoResponseDTO salvo =
    // serviceEstabelecimento.criar(estabelecimento);
    // return ResponseEntity.created(URI.create("/estabelecimento/" +
    // salvo.getIdEstabelecimento())).body(salvo);
    // }

    @GetMapping
    public ResponseEntity<List<EstabelecimentoDTO>> listarTodos() {
        return ResponseEntity.ok(service.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Estabelecimento> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.buscarPorId(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstabelecimentoDTO> atualizar(
            @PathVariable Long id,
            @RequestBody Estabelecimento estabelecimento) {
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
            @RequestParam String placeId) {
        return ResponseEntity.ok(service.vincularComPlace(id, placeId));
    }

    @GetMapping("/cupons/{idEstabelecimento}")
    public ResponseEntity<List<CupomDTO>> listarCuponsPorEstabelecimento(@PathVariable Long idEstabelecimento) {
        List<CupomDTO> cupons = service.listarCuponsPorEstabelecimento(idEstabelecimento);
        return ResponseEntity.ok(cupons);
    }

}
