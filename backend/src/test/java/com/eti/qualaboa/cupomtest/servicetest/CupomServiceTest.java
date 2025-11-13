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

/**
 * Teste unitário para o CupomService.
 * Foca em testar a lógica de negócio, "mockando" (simulando) os repositórios.
 * Não precisa de banco de dados ou Docker.
 */
@ExtendWith(MockitoExtension.class)
public class CupomServiceTest {

    // Mocks: Simulações das dependências (banco de dados)
    @Mock
    private CupomRepository cupomRepository;
    @Mock
    private EstabelecimentoRepository estabelecimentoRepository;

    // Injeta os mocks acima no serviço que queremos testar
    @InjectMocks
    private CupomService cupomService;

    // Objetos de teste reutilizáveis
    private Estabelecimento mockEstabelecimento;
    private Cupom mockCupom;
    private CupomDTO mockCupomDTO;

    @BeforeEach
    void setUp() {
        // --- ARRANGE (Preparação) ---
        // Cria um estabelecimento mockado
        mockEstabelecimento = Estabelecimento.builder()
                .idEstabelecimento(1L)
                .nome("Bar do Mock")
                .build();

        // Cria um DTO (objeto de entrada) para o cupom
        mockCupomDTO = CupomDTO.builder()
                .idCupom(1L)
                .idEstabelecimento(1L)
                .descricao("10% OFF")
                .tipo(TipoCupom.DESCONTO)
                .valor(10.0)
                .quantidadeTotal(100)
                .build();

        // Cria a entidade Cupom (o que o banco retornaria)
        mockCupom = Cupom.builder()
                .idCupom(1L)
                .estabelecimento(mockEstabelecimento) // Importante para o toDTO()
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
        // --- ARRANGE ---
        // 1. Simula a busca do estabelecimento (necessário para criar o cupom)
        when(estabelecimentoRepository.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));

        // 2. Simula o 'save' do repositório
        // Usamos thenAnswer para retornar o objeto que o serviço tentou salvar,
        // mas com um ID simulado. Isso corrige o NullPointerException do toDTO().
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(invocation -> {
            Cupom cupomSalvo = invocation.getArgument(0);
            cupomSalvo.setIdCupom(1L); // Simula o DB gerando o ID
            return cupomSalvo;
        });

        // --- ACT ---
        CupomDTO resultadoDTO = cupomService.criarCupom(mockCupomDTO);

        // --- ASSERT ---
        assertThat(resultadoDTO).isNotNull();
        assertThat(resultadoDTO.getIdEstabelecimento()).isEqualTo(1L);
        assertThat(resultadoDTO.getDescricao()).isEqualTo("10% OFF");

        // Verifica se os valores padrão foram setados pelo serviço
        assertThat(resultadoDTO.isAtivo()).isTrue();
        assertThat(resultadoDTO.getQuantidadeUsada()).isEqualTo(0);

        // Verifica se o código aleatório foi gerado
        assertThat(resultadoDTO.getCodigo()).isNotNull();
        assertThat(resultadoDTO.getCodigo()).startsWith("QLB-");

        // Verifica se os mocks foram chamados
        verify(estabelecimentoRepository, times(1)).findById(1L);
        verify(cupomRepository, times(1)).save(any(Cupom.class));
    }

    @Test
    @DisplayName("Deve falhar ao criar cupom se Estabelecimento não existir")
    void criarCupom_Falha_EstabelecimentoNaoEncontrado() {
        // --- ARRANGE ---
        // Simula que o estabelecimento 99L não foi encontrado
        when(estabelecimentoRepository.findById(99L)).thenReturn(Optional.empty());
        mockCupomDTO.setIdEstabelecimento(99L); // Seta o ID inválido no DTO

        // --- ACT & ASSERT ---
        // Verifica se a exceção correta é lançada
        assertThatThrownBy(() -> cupomService.criarCupom(mockCupomDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Estabelecimento não encontrado");

        // Garante que o cupom NUNCA foi salvo
        verify(cupomRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve listar todos os cupons")
    void listarTodos_DeveRetornarListaDeDTOs() {
        // --- ARRANGE ---
        when(cupomRepository.findAll()).thenReturn(List.of(mockCupom));

        // --- ACT ---
        List<CupomDTO> resultados = cupomService.listarTodos();

        // --- ASSERT ---
        assertThat(resultados)
                .isNotNull()
                .hasSize(1);
        assertThat(resultados.get(0).getIdCupom()).isEqualTo(1L);
        assertThat(resultados.get(0).getCodigo()).isEqualTo("QLB-ABCDE");
        // Verifica se o toDTO funcionou e mapeou o ID do estabelecimento
        assertThat(resultados.get(0).getIdEstabelecimento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve retornar lista vazia se não houver cupons")
    void listarTodos_SemCupons_DeveRetornarListaVazia() {
        // --- ARRANGE ---
        when(cupomRepository.findAll()).thenReturn(Collections.emptyList());

        // --- ACT ---
        List<CupomDTO> resultados = cupomService.listarTodos();

        // --- ASSERT ---
        assertThat(resultados)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("Deve buscar cupom por ID com sucesso")
    void buscarPorId_Encontrado() {
        // --- ARRANGE ---
        when(cupomRepository.findById(1L)).thenReturn(Optional.of(mockCupom));

        // --- ACT ---
        CupomDTO resultado = cupomService.buscarPorId(1L);

        // --- ASSERT ---
        assertThat(resultado).isNotNull();
        assertThat(resultado.getDescricao()).isEqualTo(mockCupom.getDescricao());
    }

    @Test
    @DisplayName("Deve falhar ao buscar ID inexistente")
    void buscarPorId_NaoEncontrado_DeveLancarExcecao() {
        // --- ARRANGE ---
        when(cupomRepository.findById(99L)).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> cupomService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Cupom não encontrado");
    }

    @Test
    @DisplayName("Deve atualizar um cupom com sucesso")
    void atualizarCupom_ComSucesso() {
        // --- ARRANGE ---
        // DTO com os dados atualizados
        CupomDTO dtoAtualizado = CupomDTO.builder()
                .descricao("Nova Descrição Atualizada")
                .valor(50.0)
                .ativo(false)
                .quantidadeTotal(500)
                .build();

        // Simula a busca pelo cupom existente
        when(cupomRepository.findById(1L)).thenReturn(Optional.of(mockCupom));
        // Simula o save
        when(cupomRepository.save(any(Cupom.class))).thenAnswer(inv -> inv.getArgument(0));

        // --- ACT ---
        CupomDTO resultado = cupomService.atualizarCupom(1L, dtoAtualizado);

        // --- ASSERT ---
        assertThat(resultado).isNotNull();
        assertThat(resultado.getDescricao()).isEqualTo("Nova Descrição Atualizada");
        assertThat(resultado.getValor()).isEqualTo(50.0);
        assertThat(resultado.isAtivo()).isFalse();
        assertThat(resultado.getQuantidadeTotal()).isEqualTo(500);
        // Verifica se o ID do estabelecimento (que não pode ser mudado) permanece o mesmo
        assertThat(resultado.getIdEstabelecimento()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Deve deletar um cupom com sucesso")
    void deletarCupom_ComSucesso() {
        // --- ARRANGE ---
        // Configura o mock para não fazer nada (void)
        doNothing().when(cupomRepository).deleteById(1L);

        // --- ACT ---
        cupomService.deletarCupom(1L);

        // --- ASSERT ---
        // Verifica se o método deleteById foi chamado exatamente 1 vez com o ID 1L
        verify(cupomRepository, times(1)).deleteById(1L);
    }
}