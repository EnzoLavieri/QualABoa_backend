package com.eti.qualaboa.cupom.service;

import com.eti.qualaboa.cupom.dto.CupomDTO;
import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.cupom.repository.CupomRepository;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CupomService {

    private final CupomRepository cupomRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;

    public List<CupomDTO> listarTodos() {
        return cupomRepository.findAll().stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CupomDTO buscarPorId(Long id) {
        return cupomRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Cupom não encontrado"));
    }

    public List<CupomDTO> buscarCuponsPorEstabelecimento(Long idEstabelecimento) {
        List<Cupom> cupons = cupomRepository.findByEstabelecimentoIdEstabelecimento(idEstabelecimento);
        return cupons.stream().map(this::toDTO).collect(Collectors.toList());
    }

    public CupomDTO criarCupom(CupomDTO dto) {
        Estabelecimento estabelecimento = estabelecimentoRepository.findById(dto.getIdEstabelecimento())
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        Cupom cupom = Cupom.builder()
                .codigo(gerarCodigoCupom())
                .descricao(dto.getDescricao())
                .tipo(dto.getTipo())
                .valor(dto.getValor())
                .dataInicio(dto.getDataInicio())
                .dataFim(dto.getDataFim())
                .quantidadeTotal(dto.getQuantidadeTotal())
                .quantidadeUsada(0)
                .ativo(true)
                .estabelecimento(estabelecimento)
                .build();

        return toDTO(cupomRepository.save(cupom));
    }

    public CupomDTO atualizarCupom(Long id, CupomDTO dto) {
        Cupom cupom = cupomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cupom não encontrado"));

        cupom.setDescricao(dto.getDescricao());
        cupom.setTipo(dto.getTipo());
        cupom.setValor(dto.getValor());
        cupom.setDataInicio(dto.getDataInicio());
        cupom.setDataFim(dto.getDataFim());
        cupom.setAtivo(dto.isAtivo());
        cupom.setQuantidadeTotal(dto.getQuantidadeTotal());
        cupom.setQuantidadeUsada(dto.getQuantidadeUsada());

        return toDTO(cupomRepository.save(cupom));
    }

    public void deletarCupom(Long id) {
        cupomRepository.deleteById(id);
    }

    //gera código aleatório
    private String gerarCodigoCupom() {
        return "QLB-" + java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    public void validarCupom(String codigo) {
        Cupom cupom = cupomRepository.findByCodigo(codigo).orElseThrow(() -> new RuntimeException("Cupom não encontrado"));
        cupom.setQuantidadeUsada(cupom.getQuantidadeUsada() + 1);
        cupomRepository.save(cupom);
    }


    private CupomDTO toDTO(Cupom c) {
        return CupomDTO.builder()
                .idCupom(c.getIdCupom())
                .codigo(c.getCodigo())
                .descricao(c.getDescricao())
                .tipo(c.getTipo())
                .valor(c.getValor())
                .dataInicio(c.getDataInicio())
                .dataFim(c.getDataFim())
                .ativo(c.isAtivo())
                .quantidadeTotal(c.getQuantidadeTotal())
                .quantidadeUsada(c.getQuantidadeUsada())
                .idEstabelecimento(c.getEstabelecimento().getIdEstabelecimento())
                .build();
    }
}

