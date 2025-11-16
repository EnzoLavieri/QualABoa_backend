package com.eti.qualaboa.cupomtest.servicetest;

import com.eti.qualaboa.cupom.dto.CupomDTO;
import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.cupom.repository.CupomRepository;
import com.eti.qualaboa.cupom.service.CupomService;
import com.eti.qualaboa.enums.TipoCupom;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CupomServiceTest {

    @Mock
    private CupomRepository cupomRepository;
    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    @InjectMocks
    private CupomService cupomService;

    private Estabelecimento mockEstabelecimento;
    private Cupom mockCupom;
    private CupomDTO mockCupomDTO;

    @BeforeEach
    void setUp() {

        mockEstabelecimento = Estabelecimento.builder()
                .idEstabelecimento(1L)
                .nome("Bar do Mock")
                .build();

        mockCupomDTO = CupomDTO.builder()
                .idCupom(1L)
                .idEstabelecimento(1L)
                .descricao("10% OFF")
                .tipo(TipoCupom.DESCONTO)
                .valor(10.0)
                .quantidadeTotal(100)
                .build();

        mockCupom = Cupom.builder()
                .idCupom(1L)
                .estabelecimento(mockEstabelecimento)
                .codigo("QLB-ABCDE")
                .descricao("10% OFF")
                .tipo(TipoCupom.DESCONTO)
                .valor(10.0)
                .ativo(true)
                .quantidadeTotal(100)
                .quantidadeUsada(0)
                .build();
    }

    @Test
    @DisplayName("Deve criar um cupom com sucesso")
    void criarCupom_ComSucesso() {

        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));


        when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> {
            Cupom cupomSalvo = invocation.getArgument(0);
            cupomSalvo.setIdCupom(1L);
            return cupomSalvo;
        });

        CupomDTO resultadoDTO = cupomService.criarCupom(mockCupomDTO);

        assertThat(resultadoDTO).isNotNull();
        assertThat(resultadoDTO.getIdEstabelecimento()).isEqualTo(1L);
        assertThat(resultadoDTO.getDescricao()).isEqualTo("10% OFF");

        assertThat(resultadoDTO.isAtivo()).isTrue();
        assertThat(resultadoDTO.getQuantidadeUsada()).isEqualTo(0);

        assertThat(resultadoDTO.getCodigo()).isNotNull();
        assertThat(resultadoDTO.getCodigo()).startsWith("QLB-");

        verify(estabelecimentoRepository, times(1)).findById(1L);
        verify(cupomRepository, times(1)).save(any(Cupom.class));
    }

    @Test
    @DisplayName("Deve falhar ao criar cupom se Estabelecimento não existir")
    void criarCupom_Falha_EstabelecimentoNaoEncontrado() {

        when(estabelecimentoRepository.findById(99L)).thenReturn(Optional.empty());
        mockCupomDTO.setIdEstabelecimento(99L);


        assertThatThrownBy(() -> cupomService.criarCupom(mockCupomDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Estabelecimento não encontrado");

        verify(cupomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os cupons")
    void listarTodos_DeveRetornarListaDeDTOs() {
        when(cupomRepository.findAll()).thenReturn(List.of(mockCupom));

        List<CupomDTO> resultados = cupomService.listarTodos();

        assertThat(resultados)
                .isNotNull()
                .hasSize(1);
        assertThat(resultados.get(0).getIdCupom()).isEqualTo(1L);
        assertThat(resultados.get(0).getCodigo()).isEqualTo("QLB-ABCDE");
        assertThat(resultados.get(0).getIdEstabelecimento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se não houver cupons")
    void listarTodos_SemCupons_DeveRetornarListaVazia() {
        when(cupomRepository.findAll()).thenReturn(Collections.emptyList());

        List<CupomDTO> resultados = cupomService.listarTodos();

        assertThat(resultados)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Deve buscar cupom por ID com sucesso")
    void buscarPorId_Encontrado() {
        when(cupomRepository.findById(1L)).thenReturn(Optional.of(mockCupom));

        CupomDTO resultado = cupomService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getDescricao()).isEqualTo(mockCupom.getDescricao());
    }

    @Test
    @DisplayName("Deve falhar ao buscar ID inexistente")
    void buscarPorId_NaoEncontrado_DeveLancarExcecao() {
        when(cupomRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cupomService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cupom não encontrado");
    }

    @Test
    @DisplayName("Deve atualizar um cupom com sucesso")
    void atualizarCupom_ComSucesso() {

        CupomDTO dtoAtualizado = CupomDTO.builder()
                .descricao("Nova Descrição Atualizada")
                .valor(50.0)
                .ativo(false)
                .quantidadeTotal(500)
                .build();

        when(cupomRepository.findById(1L)).thenReturn(Optional.of(mockCupom));
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(inv -> inv.getArgument(0));

        CupomDTO resultado = cupomService.atualizarCupom(1L, dtoAtualizado);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getDescricao()).isEqualTo("Nova Descrição Atualizada");
        assertThat(resultado.getValor()).isEqualTo(50.0);
        assertThat(resultado.isAtivo()).isFalse();
        assertThat(resultado.getQuantidadeTotal()).isEqualTo(500);
        assertThat(resultado.getIdEstabelecimento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve deletar um cupom com sucesso")
    void deletarCupom_ComSucesso() {

        doNothing().when(cupomRepository).deleteById(1L);

        cupomService.deletarCupom(1L);

        verify(cupomRepository, times(1)).deleteById(1L);
    }
}