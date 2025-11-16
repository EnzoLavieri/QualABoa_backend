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

@ExtendWith(MockitoExtension.class)
public class PromocaoServiceTest {

    @Mock
    private PromocaoRepository promocaoRepository;

    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @Mock
    private CupomRepository cupomRepository;

    @InjectMocks
    private PromocaoService promocaoService;

    private Estabelecimento mockEstabelecimento;
    private Cupom mockCupom;
    private Promocao mockPromocao;
    private PromocaoDTO mockPromocaoDTO;

    @BeforeEach
    void setUp() {

        mockEstabelecimento = new Estabelecimento();
        mockEstabelecimento.setIdEstabelecimento(1L);
        mockEstabelecimento.setNome("Bar Teste");

        mockCupom = new Cupom();
        mockCupom.setIdCupom(10L);
        mockCupom.setCodigo("TESTE123");

        mockPromocaoDTO = PromocaoDTO.builder()
                .idEstabelecimento(1L)
                .idCupom(10L)
                .descricao("Promoção Teste")
                .desconto(15.0)
                .ativa(true)
                .build();

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

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));

        when(cupomRepository.findById(10L)).thenReturn(Optional.of(mockCupom));

        when(promocaoRepository.save(any(Promocao.class))).thenAnswer(invocation -> {

            Promocao promocaoSalva = invocation.getArgument(0);
            promocaoSalva.setIdPromocao(100L);
            return promocaoSalva;
        });

        PromocaoDTO resultadoDTO = promocaoService.criarPromocao(mockPromocaoDTO);

        assertNotNull(resultadoDTO);
        assertEquals(100L, resultadoDTO.getIdPromocao());
        assertEquals("Promoção Teste", resultadoDTO.getDescricao());
        assertEquals(1L, resultadoDTO.getIdEstabelecimento());
        assertEquals(10L, resultadoDTO.getIdCupom());
        assertEquals(0, resultadoDTO.getTotalCliques());

        verify(estabelecimentoRepository, times(1)).findById(1L);
        verify(cupomRepository, times(1)).findById(10L);
        verify(promocaoRepository, times(1)).save(any(Promocao.class));
    }

    @Test
    @DisplayName("Deve criar promoção mesmo sem cupom")
    void deveCriarPromocaoSemCupom() {
        mockPromocaoDTO.setIdCupom(null);

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));
        when(promocaoRepository.save(any(Promocao.class))).thenAnswer(invocation -> {
            Promocao p = invocation.getArgument(0);
            p.setIdPromocao(101L);
            return p;
        });

        PromocaoDTO resultadoDTO = promocaoService.criarPromocao(mockPromocaoDTO);

        assertNotNull(resultadoDTO);
        assertNull(resultadoDTO.getIdCupom());
        assertEquals(101L, resultadoDTO.getIdPromocao());
        verify(cupomRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Deve falhar ao criar promoção se estabelecimento não existe")
    void deveFalharCriarSeEstabelecimentoNaoExiste() {

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.empty());


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            promocaoService.criarPromocao(mockPromocaoDTO);
        });

        assertEquals("Estabelecimento não encontrado", exception.getMessage());

        verify(promocaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao criar promoção se cupom não existe")
    void deveFalharCriarSeCupomNaoExiste() {
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));
        when(cupomRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            promocaoService.criarPromocao(mockPromocaoDTO);
        });

        assertEquals("Cupom não encontrado", exception.getMessage());
        verify(promocaoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar promoção por ID com sucesso")
    void deveBuscarPorId() {

        when(promocaoRepository.findById(100L)).thenReturn(Optional.of(mockPromocao));

        PromocaoDTO resultadoDTO = promocaoService.buscarPorId(100L);

        assertNotNull(resultadoDTO);
        assertEquals(100L, resultadoDTO.getIdPromocao());
        assertEquals(1L, resultadoDTO.getIdEstabelecimento());
        assertEquals("Promoção Teste", resultadoDTO.getDescricao());
    }

    @Test
    @DisplayName("Deve falhar ao buscar ID inexistente")
    void deveFalharBuscarIdInexistente() {
        when(promocaoRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            promocaoService.buscarPorId(999L);
        });
        assertEquals("Promoção não encontrada", exception.getMessage());
    }

    @Test
    @DisplayName("Deve listar todas as promoções")
    void deveListarTodas() {
        when(promocaoRepository.findAll()).thenReturn(List.of(mockPromocao));

        List<PromocaoDTO> resultados = promocaoService.listarTodas();

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertEquals(100L, resultados.get(0).getIdPromocao());
    }

    @Test
    @DisplayName("Deve deletar uma promoção")
    void deveDeletarPromocao() {

        doNothing().when(promocaoRepository).deleteById(100L);

        promocaoService.deletarPromocao(100L);


        verify(promocaoRepository, times(1)).deleteById(100L);
    }
}