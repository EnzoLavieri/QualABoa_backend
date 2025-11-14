package com.eti.qualaboa.estabelecimentotest.servicetest;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoRegisterDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoResponseDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import com.eti.qualaboa.map.places.PlacesClient;
import com.eti.qualaboa.metricas.service.MetricasService;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EstabelecimentoServiceTest {

    // Mocks para todas as 5 dependências
    @Mock
    private EstabelecimentoRepository repositoryEstabelecimento;
    @Mock
    private PlacesClient placesClient;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private MetricasService metricasService;

    // Injeta os mocks acima no serviço
    @InjectMocks
    private EstabelecimentoService estabelecimentoService;

    // Dados de teste reutilizáveis
    private EstabelecimentoRegisterDTO registerDTO;
    private Role mockRole;
    private Estabelecimento mockEstabelecimento;

    @BeforeEach
    void setUp() {
        // DTO para registrar um novo estabelecimento
        registerDTO = new EstabelecimentoRegisterDTO();
        registerDTO.setNome("Bar Novo Teste");
        registerDTO.setEmail("novo@teste.com");
        registerDTO.setSenha("senha123");
        registerDTO.setIdRole(3L);
        registerDTO.setLatitude(-23.0);
        registerDTO.setLongitude(-51.0);

        // Mock da Role
        mockRole = new Role();
        mockRole.setId(3L);
        mockRole.setNome("ESTABELECIMENTO");

        // Mock da Entidade Estabelecimento
        mockEstabelecimento = Estabelecimento.builder()
                .idEstabelecimento(1L)
                .nome("Bar Já Salvo")
                .email("ja@salvo.com")
                .parceiro(true)
                .build();
    }

    @Test
    @DisplayName("Deve criar um estabelecimento com sucesso")
    void criar_ComSucesso() {
        // --- ARRANGE ---

        // 1. Simula que o email NÃO existe
        when(repositoryEstabelecimento.existsByEmail("novo@teste.com")).thenReturn(false);

        // 2. Simula o encoder de senha
        when(passwordEncoder.encode("senha123")).thenReturn("senhaCriptografada");

        // 3. Simula a busca pela Role
        when(roleRepository.findByNome("ESTABELECIMENTO")).thenReturn(Optional.of(mockRole));

        // 4. Simula o 'save' do repositório (usando thenAnswer para setar o ID)
        when(repositoryEstabelecimento.save(any(Estabelecimento.class))).thenAnswer(invocation -> {
            Estabelecimento estSalvo = invocation.getArgument(0);
            estSalvo.setIdEstabelecimento(1L); // Simula o DB gerando o ID
            return estSalvo;
        });

        // 5. Simula as chamadas JDBC (não precisam fazer nada)
        doNothing().when(jdbcTemplate).execute(anyString());
        when(jdbcTemplate.update(anyString(), anyDouble(), anyDouble(), anyLong())).thenReturn(1);

        // --- ACT ---
        EstabelecimentoResponseDTO response = estabelecimentoService.criar(registerDTO);

        // --- ASSERT ---
        assertThat(response).isNotNull();
        assertThat(response.getIdEstabelecimento()).isEqualTo(1L);
        assertThat(response.getNome()).isEqualTo("Bar Novo Teste");
        assertThat(response.getEmail()).isEqualTo("novo@teste.com");

        // Verifica se os mocks corretos foram chamados
        verify(passwordEncoder, times(1)).encode("senha123");
        verify(roleRepository, times(1)).findByNome("ESTABELECIMENTO");
        verify(repositoryEstabelecimento, times(1)).save(any(Estabelecimento.class));
        verify(jdbcTemplate, times(1)).execute(anyString()); // Verifica se tentou criar/alterar a col geom
        verify(jdbcTemplate, times(1)).update(anyString(), eq(-51.0), eq(-23.0), eq(1L)); // Verifica se atualizou o geom
    }

    @Test
    @DisplayName("Deve falhar ao criar se o email já existir")
    void criar_Falha_EmailJaExiste() {
        // --- ARRANGE ---
        when(repositoryEstabelecimento.existsByEmail("novo@teste.com")).thenReturn(true);

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> estabelecimentoService.criar(registerDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("E-mail já cadastrado");

        // Garante que nada foi salvo
        verify(repositoryEstabelecimento, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar ao criar se a Role 'ESTABELECIMENTO' não for encontrada")
    void criar_Falha_RoleNaoEncontrada() {
        // --- ARRANGE ---
        when(repositoryEstabelecimento.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(roleRepository.findByNome("ESTABELECIMENTO")).thenReturn(Optional.empty());

        // --- ACT & ASSERT ---
        assertThatThrownBy(() -> estabelecimentoService.criar(registerDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Role ESTABELECIMENTO não encontrada");

        verify(repositoryEstabelecimento, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar por ID com sucesso")
    void buscarPorId_Sucesso() {
        when(repositoryEstabelecimento.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));

        doNothing().when(metricasService).registrarClique(1L);
        Estabelecimento resultado = estabelecimentoService.buscarPorId(1L);

        assertThat(resultado).isNotNull();
        assertThat(resultado.getNome()).isEqualTo("Bar Já Salvo");
    }

    @Test
    @DisplayName("Deve falhar ao buscar ID que não existe")
    void buscarPorId_Falha_NaoEncontrado() {
        when(repositoryEstabelecimento.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> estabelecimentoService.buscarPorId(99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Estabelecimento não encontrado");
    }

    @Test
    @DisplayName("Deve vincular um Estabelecimento a um PlaceId do Google")
    void vincularComPlace_Sucesso() {
        // --- ARRANGE ---
        String placeId = "place_teste_123";
        Estabelecimento estSemPlace = Estabelecimento.builder().idEstabelecimento(1L).nome("Bar Antigo").build();

        // 1. Mock do findById
        when(repositoryEstabelecimento.findById(1L)).thenReturn(Optional.of(estSemPlace));

        // 2. Mock da resposta do PlacesClient
        Map<String, Object> mockLocation = Map.of("lat", -23.1, "lng", -51.1);
        Map<String, Object> mockGeometry = Map.of("location", mockLocation);
        Map<String, Object> mockResult = Map.of(
                "name", "Bar do Google",
                "formatted_address", "Rua Google, 123",
                "geometry", mockGeometry
        );
        Map<String, Object> mockDetails = Map.of("result", mockResult);
        when(placesClient.placeDetails(placeId)).thenReturn(mockDetails);

        // 3. Mock do save
        when(repositoryEstabelecimento.save(any(Estabelecimento.class))).thenAnswer(inv -> inv.getArgument(0));

        // --- ACT ---
        EstabelecimentoDTO dtoResultado = estabelecimentoService.vincularComPlace(1L, placeId);

        // --- ASSERT ---
        assertThat(dtoResultado).isNotNull();
        assertThat(dtoResultado.getNome()).isEqualTo("Bar do Google");
        assertThat(dtoResultado.getPlaceId()).isEqualTo(placeId);
        assertThat(dtoResultado.getLatitude()).isEqualTo(-23.1);
        assertThat(dtoResultado.getLongitude()).isEqualTo(-51.1);
        assertThat(dtoResultado.getEnderecoFormatado()).isEqualTo("Rua Google, 123");
        assertThat(dtoResultado.getParceiro()).isTrue();

        verify(repositoryEstabelecimento, times(1)).save(any(Estabelecimento.class));
    }

    @Test
    @DisplayName("Deve listar cupons de um estabelecimento")
    void listarCuponsPorEstabelecimento_Sucesso() {
        // --- ARRANGE ---
        Cupom cupom1 = Cupom.builder().idCupom(10L).codigo("CUPOM10").build();
        Cupom cupom2 = Cupom.builder().idCupom(20L).codigo("CUPOM20").build();

        // Seta a lista de cupons no estabelecimento mockado
        mockEstabelecimento.setCupons(List.of(cupom1, cupom2));

        when(repositoryEstabelecimento.findById(1L)).thenReturn(Optional.of(mockEstabelecimento));

        // --- ACT ---
        List<com.eti.qualaboa.cupom.dto.CupomDTO> cupons = estabelecimentoService.listarCuponsPorEstabelecimento(1L);

        // --- ASSERT ---
        assertThat(cupons)
                .isNotNull()
                .hasSize(2);
        assertThat(cupons.get(0).getCodigo()).isEqualTo("CUPOM10");
        assertThat(cupons.get(1).getIdEstabelecimento()).isEqualTo(1L); // Verifica se o ID do Est. foi setado no DTO
    }
}