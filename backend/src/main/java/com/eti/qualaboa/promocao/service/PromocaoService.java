package com.eti.qualaboa.promocao.service;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.cupom.repository.CupomRepository;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.promocao.dto.PromocaoDTO;
import com.eti.qualaboa.promocao.model.Promocao;
import com.eti.qualaboa.promocao.repository.PromocaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PromocaoService {

    private final PromocaoRepository promocaoRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final CupomRepository cupomRepository;

    public List<PromocaoDTO> listarTodas() {
        return promocaoRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public PromocaoDTO buscarPorId(Long id) {
        return promocaoRepository.findById(id)
                .map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Promoção não encontrada"));
    }

    public PromocaoDTO criarPromocao(PromocaoDTO dto) {
        Estabelecimento est = estabelecimentoRepository.findById(dto.getIdEstabelecimento())
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        Cupom cupom = null;
        if (dto.getIdCupom() != null) {
            cupom = cupomRepository.findById(dto.getIdCupom())
                    .orElseThrow(() -> new RuntimeException("Cupom não encontrado"));
        }

        Promocao promocao = Promocao.builder()
                .descricao(dto.getDescricao())
                .desconto(dto.getDesconto())
                .totalVisualizacoes(0)
                .totalCliques(0)
                .ativa(true)
                .estabelecimento(est)
                .cupom(cupom)
                .build();

        return toDTO(promocaoRepository.save(promocao));
    }

    public PromocaoDTO atualizarPromocao(Long id, PromocaoDTO dto) {
        Promocao promocao = promocaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Promoção não encontrada"));

        promocao.setDescricao(dto.getDescricao());
        promocao.setDesconto(dto.getDesconto());
        promocao.setTotalVisualizacoes(dto.getTotalVisualizacoes());
        promocao.setTotalCliques(dto.getTotalCliques());
        promocao.setAtiva(dto.isAtiva());

        return toDTO(promocaoRepository.save(promocao));
    }

    public void deletarPromocao(Long id) {
        promocaoRepository.deleteById(id);
    }

    private PromocaoDTO toDTO(Promocao p) {
        return PromocaoDTO.builder()
                .idPromocao(p.getIdPromocao())
                .idEstabelecimento(p.getEstabelecimento().getIdEstabelecimento())
                .idCupom(p.getCupom() != null ? p.getCupom().getIdCupom() : null)
                .descricao(p.getDescricao())
                .desconto(p.getDesconto())
                .totalVisualizacoes(p.getTotalVisualizacoes())
                .totalCliques(p.getTotalCliques())
                .ativa(p.isAtiva())
                .build();
    }
}
