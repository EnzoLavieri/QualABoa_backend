package com.eti.qualaboa.eventotest.servicetest;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.cupom.repository.CupomRepository;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.evento.dto.EventoDTO;
import com.eti.qualaboa.evento.model.Evento;
import com.eti.qualaboa.evento.repository.EventoRepository;
import com.eti.qualaboa.evento.service.EventoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;
    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;
    @Mock
    private CupomRepository cupomRepository;

    @InjectMocks
    private EventoService eventoService;

    private Estabelecimento mockEstabelecimento;
    private Cupom mockCupom;
    private Evento mockEvento;
    private EventoDTO mockEventoDTO;

    @BeforeEach
    void setUp() {

        mockEstabelecimento = Estabelecimento.builder()
                .idEstabelecimento(1L)
                .nome("Bar Teste")
                .build();

        mockCupom = Cupom.builder()
                .idCupom(1L)
                .codigo("TESTE10")
                .estabelecimento(mockEstabelecimento)
                .build();

        mockEventoDTO = EventoDTO.builder()
                .idEvento(1L)
                .idEstabelecimento(1L)
                .idCupom(1L)
                .titulo("Show de Teste")
                .descricao("Descrição do show")
                .data("2025-12-25")
                .horario("20:00")
                .build();

        mockEvento = Evento.builder()
                .idEvento(1L)
                .estabelecimento(mockEstabelecimento)
                .cupom(mockCupom)
                .titulo("Show de Teste")
                .descricao("Descrição do show")
                .data("2025-12-25")
                .horario("20:00")
                .totalCliques(0)
                .totalVisualizacoes(0)
                .build();
    }

    @Test
    @DisplayName("Deve criar um evento com sucesso (com cupom)")
    void criarEvento_ComSucesso_ComCupom() {

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));
        when(cupomRepository.findById(1L)).thenReturn(Optional.of(mockCupom));


        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
            Evento eventoSalvo = invocation.getArgument(0);
            eventoSalvo.setIdEvento(1L); // Simula o DB gerando um ID
            return eventoSalvo;
        });

        EventoDTO resultadoDTO = eventoService.criarEvento(mockEventoDTO);

        assertThat(resultadoDTO).isNotNull();
        assertThat(resultadoDTO.getTitulo()).isEqualTo("Show de Teste");
        assertThat(resultadoDTO.getIdEstabelecimento()).isEqualTo(1L);
        assertThat(resultadoDTO.getIdCupom()).isEqualTo(1L);
        assertThat(resultadoDTO.getTotalCliques()).isEqualTo(0); // Verifica valor default
    }

    @Test
    @DisplayName("Deve criar um evento com sucesso (sem cupom)")
    void criarEvento_ComSucesso_SemCupom() {
        mockEventoDTO.setIdCupom(null);
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));

        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
            Evento eventoSalvo = invocation.getArgument(0);
            eventoSalvo.setIdEvento(1L);
            return eventoSalvo;
        });

        EventoDTO resultadoDTO = eventoService.criarEvento(mockEventoDTO);

        assertThat(resultadoDTO).isNotNull();
        assertThat(resultadoDTO.getIdCupom()).isNull();
        verify(cupomRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve falhar ao criar evento se Estabelecimento não existir")
    void criarEvento_Falha_EstabelecimentoNaoEncontrado() {

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> eventoService.criarEvento(mockEventoDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Estabelecimento não encontrado");
    }

    @Test
    @DisplayName("Deve falhar ao criar evento se Cupom (opcional) não existir")
    void criarEvento_Falha_CupomNaoEncontrado() {

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));
        when(cupomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.criarEvento(mockEventoDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cupom não encontrado");
    }

    @Test
    @DisplayName("Deve listar todos os eventos")
    void listarTodos_DeveRetornarListaDeDTOs() {
        when(eventoRepository.findAll()).thenReturn(List.of(mockEvento));

        List<EventoDTO> resultados = eventoService.listarTodos();

        assertThat(resultados)
                .isNotNull()
                .hasSize(1);
        assertThat(resultados.get(0).getIdEvento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se não houver eventos")
    void listarTodos_SemEventos_DeveRetornarListaVazia() {
        when(eventoRepository.findAll()).thenReturn(Collections.emptyList());

        List<EventoDTO> resultados = eventoService.listarTodos();

        assertThat(resultados)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Deve buscar evento por ID com sucesso")
    void buscarPorId_Encontrado() {
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(mockEvento));

        EventoDTO resultado = eventoService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo(mockEvento.getTitulo());
    }

    @Test
    @DisplayName("Deve falhar ao buscar ID inexistente")
    void buscarPorId_NaoEncontrado_DeveLancarExcecao() {
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Evento não encontrado");
    }

    @Test
    @DisplayName("Deve atualizar um evento com sucesso")
    void atualizarEvento_ComSucesso() {

        EventoDTO dtoAtualizado = EventoDTO.builder()
                .titulo("Novo Título")
                .descricao("Nova Descrição")
                .data("2099-01-01")
                .horario("18:00")
                .totalVisualizacoes(100)
                .totalCliques(10)
                .build();

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(mockEvento));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        EventoDTO resultado = eventoService.atualizarEvento(1L, dtoAtualizado);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Novo Título");
        assertThat(resultado.getDescricao()).isEqualTo("Nova Descrição");
        assertThat(resultado.getData()).isEqualTo("2099-01-01");
        assertThat(resultado.getTotalCliques()).isEqualTo(10);
        assertThat(resultado.getIdEstabelecimento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve deletar um evento com sucesso")
    void deletarEvento_ComSucesso() {

        doNothing().when(eventoRepository).deleteById(1L);

        eventoService.deletarEvento(1L);


        verify(eventoRepository, times(1)).deleteById(1L);
    }
}