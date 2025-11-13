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

/**
 * Teste unitário para o EventoService.
 * Foca em testar a lógica de negócio, "mockando" (simulando) os repositórios.
 * Não precisa de banco de dados ou Docker.
 */
@ExtendWith(MockitoExtension.class)
public class EventoServiceTest {

    // Mocks: Simulações das dependências (banco de dados)
    @Mock
    private EventoRepository eventoRepository;
    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;
    @Mock
    private CupomRepository cupomRepository;

    // Injeta os mocks acima no serviço que queremos testar
    @InjectMocks
    private EventoService eventoService;

    // Objetos de teste reutilizáveis
    private Estabelecimento mockEstabelecimento;
    private Cupom mockCupom;
    private Evento mockEvento;
    private EventoDTO mockEventoDTO;

    @BeforeEach
    void setUp() {
        // --- ARRANGE (Preparação) ---
        // Cria um estabelecimento mockado
        mockEstabelecimento = Estabelecimento.builder()
                .idEstabelecimento(1L)
                .nome("Bar Teste")
                .build();

        // Cria um cupom mockado
        mockCupom = Cupom.builder()
                .idCupom(1L)
                .codigo("TESTE10")
                .estabelecimento(mockEstabelecimento)
                .build();

        // Cria um DTO (objeto de entrada) para o evento
        mockEventoDTO = EventoDTO.builder()
                .idEvento(1L)
                .idEstabelecimento(1L)
                .idCupom(1L)
                .titulo("Show de Teste")
                .descricao("Descrição do show")
                .data("2025-12-25")
                .horario("20:00")
                .build();

        // Cria a entidade Evento (o que o banco retornaria)
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
        // --- ARRANGE ---
        // Configura os mocks para encontrar o estabelecimento e o cupom
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));
        when(cupomRepository.findById(1L)).thenReturn(Optional.of(mockCupom));

        // Configura o mock do save para retornar o evento (simulando o salvamento)
        // Usamos thenAnswer para retornar o objeto que foi passado para o save
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
            Evento eventoSalvo = invocation.getArgument(0);
            eventoSalvo.setIdEvento(1L); // Simula o DB gerando um ID
            return eventoSalvo;
        });

        // --- ACT ---
        EventoDTO resultadoDTO = eventoService.criarEvento(mockEventoDTO);

        // --- ASSERT ---
        assertThat(resultadoDTO).isNotNull();
        assertThat(resultadoDTO.getTitulo()).isEqualTo("Show de Teste");
        assertThat(resultadoDTO.getIdEstabelecimento()).isEqualTo(1L);
        assertThat(resultadoDTO.getIdCupom()).isEqualTo(1L);
        assertThat(resultadoDTO.getTotalCliques()).isEqualTo(0); // Verifica valor default
    }

    @Test
    @DisplayName("Deve criar um evento com sucesso (sem cupom)")
    void criarEvento_ComSucesso_SemCupom() {
        // --- ARRANGE ---
        mockEventoDTO.setIdCupom(null); // Modifica o DTO para não ter cupom
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));

        // Configura o save
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> {
            Evento eventoSalvo = invocation.getArgument(0);
            eventoSalvo.setIdEvento(1L);
            return eventoSalvo;
        });

        // --- ACT ---
        EventoDTO resultadoDTO = eventoService.criarEvento(mockEventoDTO);

        // --- ASSERT ---
        assertThat(resultadoDTO).isNotNull();
        assertThat(resultadoDTO.getIdCupom()).isNull();
        // Verifica se o cupomRepository NUNCA foi chamado
        verify(cupomRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Deve falhar ao criar evento se Estabelecimento não existir")
    void criarEvento_Falha_EstabelecimentoNaoEncontrado() {
        // --- ARRANGE ---
        // Simula que o estabelecimento não foi encontrado
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        // Verifica se a exceção correta é lançada
        assertThatThrownBy(() -> eventoService.criarEvento(mockEventoDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Estabelecimento não encontrado");
    }

    @Test
    @DisplayName("Deve falhar ao criar evento se Cupom (opcional) não existir")
    void criarEvento_Falha_CupomNaoEncontrado() {
        // --- ARRANGE ---
        // Simula que o estabelecimento FOI encontrado
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));
        // Mas o cupom (ID 1) não foi
        when(cupomRepository.findById(1L)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> eventoService.criarEvento(mockEventoDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cupom não encontrado");
    }

    @Test
    @DisplayName("Deve listar todos os eventos")
    void listarTodos_DeveRetornarListaDeDTOs() {
        // --- ARRANGE ---
        when(eventoRepository.findAll()).thenReturn(List.of(mockEvento));

        // --- ACT ---
        List<EventoDTO> resultados = eventoService.listarTodos();

        // --- ASSERT ---
        assertThat(resultados)
                .isNotNull()
                .hasSize(1);
        assertThat(resultados.get(0).getIdEvento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se não houver eventos")
    void listarTodos_SemEventos_DeveRetornarListaVazia() {
        // --- ARRANGE ---
        when(eventoRepository.findAll()).thenReturn(Collections.emptyList());

        // --- ACT ---
        List<EventoDTO> resultados = eventoService.listarTodos();

        // --- ASSERT ---
        assertThat(resultados)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Deve buscar evento por ID com sucesso")
    void buscarPorId_Encontrado() {
        // --- ARRANGE ---
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(mockEvento));

        // --- ACT ---
        EventoDTO resultado = eventoService.buscarPorId(1L);

        // --- ASSERT ---
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo(mockEvento.getTitulo());
    }

    @Test
    @DisplayName("Deve falhar ao buscar ID inexistente")
    void buscarPorId_NaoEncontrado_DeveLancarExcecao() {
        // --- ARRANGE ---
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> eventoService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Evento não encontrado");
    }

    @Test
    @DisplayName("Deve atualizar um evento com sucesso")
    void atualizarEvento_ComSucesso() {
        // --- ARRANGE ---
        // DTO com os dados atualizados
        EventoDTO dtoAtualizado = EventoDTO.builder()
                .titulo("Novo Título")
                .descricao("Nova Descrição")
                .data("2099-01-01")
                .horario("18:00")
                .totalVisualizacoes(100)
                .totalCliques(10)
                .build();

        // Simula a busca pelo evento existente
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(mockEvento));
        // Simula o save
        when(eventoRepository.save(any(Evento.class))).thenAnswer(inv -> inv.getArgument(0));

        // --- ACT ---
        EventoDTO resultado = eventoService.atualizarEvento(1L, dtoAtualizado);

        // --- ASSERT ---
        assertThat(resultado).isNotNull();
        assertThat(resultado.getTitulo()).isEqualTo("Novo Título");
        assertThat(resultado.getDescricao()).isEqualTo("Nova Descrição");
        assertThat(resultado.getData()).isEqualTo("2099-01-01");
        assertThat(resultado.getTotalCliques()).isEqualTo(10);
        // Verifica se o ID do estabelecimento (que não pode ser mudado) permanece o mesmo
        assertThat(resultado.getIdEstabelecimento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve deletar um evento com sucesso")
    void deletarEvento_ComSucesso() {
        // --- ARRANGE ---
        // Configura o mock para não fazer nada (void)
        doNothing().when(eventoRepository).deleteById(1L);

        // --- ACT ---
        eventoService.deletarEvento(1L);

        // --- ASSERT ---
        // Verifica se o método deleteById foi chamado exatamente 1 vez com o ID 1L
        verify(eventoRepository, times(1)).deleteById(1L);
    }
}