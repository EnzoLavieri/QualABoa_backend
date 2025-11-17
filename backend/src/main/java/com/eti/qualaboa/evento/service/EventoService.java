package com.eti.qualaboa.evento.service;

import com.eti.qualaboa.evento.dto.EventoDTO;
import com.eti.qualaboa.evento.model.Evento;
import com.eti.qualaboa.evento.repository.EventoRepository;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.cupom.repository.CupomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;
    private final EstabelecimentoRepository estabelecimentoRepository;
    private final CupomRepository cupomRepository;

    public List<EventoDTO> listarTodos() {
        return eventoRepository.findAll()
                .stream().map(this::toDTO).collect(Collectors.toList());
    }

    public EventoDTO buscarPorId(Long id) {
        return eventoRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));
    }

    public EventoDTO criarEvento(EventoDTO dto) {
        Estabelecimento est = estabelecimentoRepository.findById(dto.getIdEstabelecimento())
                .orElseThrow(() -> new RuntimeException("Estabelecimento não encontrado"));

        Cupom cupom = null;
        if (dto.getIdCupom() != null) {
            cupom = cupomRepository.findById(dto.getIdCupom())
                    .orElseThrow(() -> new RuntimeException("Cupom não encontrado"));
        }

        Evento evento = Evento.builder()
                .descricao(dto.getDescricao())
                .titulo(dto.getTitulo())
                .data(dto.getData())
                .horario(dto.getHorario())
                .totalVisualizacoes(0)
                .totalCliques(0)
                .estabelecimento(est)
                .cupom(cupom)
                .build();

        return toDTO(eventoRepository.save(evento));
    }

    public EventoDTO atualizarEvento(Long id, EventoDTO dto) {
        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento não encontrado"));

        evento.setDescricao(dto.getDescricao());
        evento.setTitulo(dto.getTitulo());
        evento.setData(dto.getData());
        evento.setHorario(dto.getHorario());
        evento.setTotalVisualizacoes(dto.getTotalVisualizacoes());
        evento.setTotalCliques(dto.getTotalCliques());

        return toDTO(eventoRepository.save(evento));
    }

    public void deletarEvento(Long id) {
        eventoRepository.deleteById(id);
    }

    private EventoDTO toDTO(Evento e) {
        return EventoDTO.builder()
                .idEvento(e.getIdEvento())
                .idEstabelecimento(e.getEstabelecimento().getIdEstabelecimento())
                .idCupom(e.getCupom() != null ? e.getCupom().getIdCupom() : null)
                .descricao(e.getDescricao())
                .titulo(e.getTitulo())
                .data(e.getData())
                .horario(e.getHorario())
                .totalVisualizacoes(e.getTotalVisualizacoes())
                .totalCliques(e.getTotalCliques())
                .build();
    }
}

