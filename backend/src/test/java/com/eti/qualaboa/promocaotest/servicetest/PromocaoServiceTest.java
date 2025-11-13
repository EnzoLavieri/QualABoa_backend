package com.eti.qualaboa.promocaotest.servicetest;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.cupom.repository.CupomRepository;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.promocao.dto.PromocaoDTO;
import com.eti.qualaboa.promocao.model.Promocao;
import com.eti.qualaboa.promocao.repository.PromocaoRepository;
import com.eti.qualaboa.promocao.service.PromocaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // Habilita o Mockito para este teste
public class PromocaoServiceTest {

    // Cria um mock (dublê) para cada dependência do serviço
    @Mock
    private PromocaoRepository promocaoRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private CupomRepository cupomRepository;

    // Injeta os mocks acima na instância real do serviço
    @InjectMocks
    private PromocaoService promocaoService;

    // --- Dados de Teste ---
    private Estabelecimento mockEstabelecimento;
    private Cupom mockCupom;
    private Promocao mockPromocao;
    private PromocaoDTO mockPromocaoDTO;

    @BeforeEach
    void setUp() {
        // Prepara dados que serão reutilizados em vários testes

        mockEstabelecimento = new Estabelecimento();
        mockEstabelecimento.setIdEstabelecimento(1L);
        mockEstabelecimento.setNome("Bar Teste");

        mockCupom = new Cupom();
        mockCupom.setIdCupom(10L);
        mockCupom.setCodigo("TESTE123");

        // DTO (Data Transfer Object) - usado para entrada e saída
        mockPromocaoDTO = PromocaoDTO.builder()
                .idEstabelecimento(1L)
                .idCupom(10L)
                .descricao("Promoção Teste")
                .desconto(15.0)
                .ativa(true)
                .build();

        // Entidade (o que é salvo no banco)
        mockPromocao = Promocao.builder()
                .idPromocao(100L)
                .estabelecimento(mockEstabelecimento)
                .cupom(mockCupom)
                .descricao("Promoção Teste")
                .desconto(15.0)
                .ativa(true)
                .totalCliques(0)
                .totalVisualizacoes(0)
                .build();
    }

    @Test
    @DisplayName("Deve criar uma promoção com sucesso")
    void deveCriarPromocaoComSucesso() {
        // --- ARRANGE (Preparação) ---

        // 1. Quando o serviço procurar o estabelecimento 1L, encontre o mockEstabelecimento
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));

        // 2. Quando o serviço procurar o cupom 10L, encontre o mockCupom
        when(cupomRepository.findById(10L)).thenReturn(Optional.of(mockCupom));

        // 3. Quando o serviço tentar salvar a promoção:
        when(promocaoRepository.save(any(Promocao.class))).thenAnswer(invocation -> {
            // Retorna a própria promoção que foi passada para o 'save',
            // simulando que ela foi salva e teve um ID atribuído.
            Promocao promocaoSalva = invocation.getArgument(0);
            promocaoSalva.setIdPromocao(100L); // Simula o ID gerado pelo banco
            return promocaoSalva;
        });

        // --- ACT (Ação) ---
        PromocaoDTO resultadoDTO = promocaoService.criarPromocao(mockPromocaoDTO);

        // --- ASSERT (Verificação) ---
        assertNotNull(resultadoDTO);
        assertEquals(100L, resultadoDTO.getIdPromocao());
        assertEquals("Promoção Teste", resultadoDTO.getDescricao());
        assertEquals(1L, resultadoDTO.getIdEstabelecimento());
        assertEquals(10L, resultadoDTO.getIdCupom());
        assertEquals(0, resultadoDTO.getTotalCliques()); // Verifica valor default

        // Verifica se os métodos dos repositórios foram chamados
        verify(estabelecimentoRepository, times(1)).findById(1L);
        verify(cupomRepository, times(1)).findById(10L);
        verify(promocaoRepository, times(1)).save(any(Promocao.class));
    }

    @Test
    @DisplayName("Deve criar promoção mesmo sem cupom")
    void deveCriarPromocaoSemCupom() {
        // --- ARRANGE ---
        mockPromocaoDTO.setIdCupom(null); // DTO não tem cupom

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));
        when(promocaoRepository.save(any(Promocao.class))).thenAnswer(invocation -> {
            Promocao p = invocation.getArgument(0);
            p.setIdPromocao(101L);
            return p;
        });

        // --- ACT ---
        PromocaoDTO resultadoDTO = promocaoService.criarPromocao(mockPromocaoDTO);

        // --- ASSERT ---
        assertNotNull(resultadoDTO);
        assertNull(resultadoDTO.getIdCupom()); // Garante que o ID do cupom é nulo
        assertEquals(101L, resultadoDTO.getIdPromocao());
        // Verifica que o findById do cupom NUNCA foi chamado
        verify(cupomRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Deve falhar ao criar promoção se estabelecimento não existe")
    void deveFalharCriarSeEstabelecimentoNaoExiste() {
        // --- ARRANGE ---
        // Simula que o estabelecimento 1L não foi encontrado
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        // Verifica se a exceção RuntimeException foi lançada
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            promocaoService.criarPromocao(mockPromocaoDTO);
        });

        // Verifica a mensagem da exceção
        assertEquals("Estabelecimento não encontrado", exception.getMessage());

        // Garante que o save nunca foi chamado
        verify(promocaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao criar promoção se cupom não existe")
    void deveFalharCriarSeCupomNaoExiste() {
        // --- ARRANGE ---
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));
        // Simula que o cupom 10L não foi encontrado
        when(cupomRepository.findById(10L)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            promocaoService.criarPromocao(mockPromocaoDTO);
        });

        assertEquals("Cupom não encontrado", exception.getMessage());
        verify(promocaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar promoção por ID com sucesso")
    void deveBuscarPorId() {
        // --- ARRANGE ---
        // Simula a busca no banco
        when(promocaoRepository.findById(100L)).thenReturn(Optional.of(mockPromocao));

        // --- ACT ---
        PromocaoDTO resultadoDTO = promocaoService.buscarPorId(100L);

        // --- ASSERT ---
        assertNotNull(resultadoDTO);
        assertEquals(100L, resultadoDTO.getIdPromocao());
        assertEquals(1L, resultadoDTO.getIdEstabelecimento());
        assertEquals("Promoção Teste", resultadoDTO.getDescricao());
    }

    @Test
    @DisplayName("Deve falhar ao buscar ID inexistente")
    void deveFalharBuscarIdInexistente() {
        // --- ARRANGE ---
        when(promocaoRepository.findById(999L)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            promocaoService.buscarPorId(999L);
        });
        assertEquals("Promoção não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar todas as promoções")
    void deveListarTodas() {
        // --- ARRANGE ---
        when(promocaoRepository.findAll()).thenReturn(List.of(mockPromocao));

        // --- ACT ---
        List<PromocaoDTO> resultados = promocaoService.listarTodas();

        // --- ASSERT ---
        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals(100L, resultados.get(0).getIdPromocao());
    }

    @Test
    @DisplayName("Deve deletar uma promoção")
    void deveDeletarPromocao() {
        // --- ARRANGE ---
        // Configura o mock para não fazer nada quando deleteById for chamado
        doNothing().when(promocaoRepository).deleteById(100L);

        // --- ACT ---
        promocaoService.deletarPromocao(100L);

        // --- ASSERT ---
        // Verifica se o método deleteById foi chamado exatamente 1 vez com o ID 100
        verify(promocaoRepository, times(1)).deleteById(100L);
    }
}