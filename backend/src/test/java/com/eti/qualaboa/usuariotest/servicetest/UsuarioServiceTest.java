package com.eti.qualaboa.usuariotest.servicetest;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.dto.UsuarioRequestDTO;
import com.eti.qualaboa.usuario.dto.UsuarioResponseDTO;
import com.eti.qualaboa.usuario.dto.UsuarioUpdateRequestDTO;
import com.eti.qualaboa.usuario.dto.UsuarioUpdateResponseDTO;
import com.eti.qualaboa.usuario.repository.RoleRepository;
import com.eti.qualaboa.usuario.repository.UsuarioRepository;
import com.eti.qualaboa.usuario.service.UsuarioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Habilita o Mockito para este teste
@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    // Cria um mock (um "dublê") para cada dependência do UsuarioService
    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EstabelecimentoService estabelecimentoService;

    // Injeta os mocks acima no UsuarioService
    @InjectMocks
    private UsuarioService usuarioService;

    // Variáveis que usaremos em múltiplos testes
    private UsuarioRequestDTO requestDTO;
    private Usuario usuario;
    private Role roleUser;

    @BeforeEach
    void setUp() {
        // --- ARRANGE (Preparação) ---
        // Prepara um DTO de requisição padrão para os testes
        requestDTO = new UsuarioRequestDTO();
        requestDTO.setNome("Teste User");
        requestDTO.setEmail("teste@user.com");
        requestDTO.setSenha("123456");
        requestDTO.setIdRole(1L); // 1L = USER
        requestDTO.setPreferenciasUsuario(List.of("pizza", "sushi"));

        // Prepara uma Role "USER"
        roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setNome("USER");

        // Prepara uma entidade Usuario padrão
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNome(requestDTO.getNome());
        usuario.setEmail(requestDTO.getEmail());
        usuario.setSenha("senha_criptografada");
        usuario.setPreferenciasUsuario(requestDTO.getPreferenciasUsuario());
        usuario.setRoles(Set.of(roleUser));
    }

    @Test
    @DisplayName("Deve criar um usuário com sucesso")
    void criarUsuario_ComSucesso() {
        // --- ARRANGE (Preparação) ---

        // 1. Simula o comportamento: Quando o repository.findByEmail for chamado, retorna vazio (email não existe)
        when(usuarioRepository.findByEmail("teste@user.com")).thenReturn(Optional.empty());

        // 2. Simula o comportamento: Quando o roleRepository.findByNome("USER") for chamado, retorna a role USER
        when(roleRepository.findByNome("USER")).thenReturn(Optional.of(roleUser));

        // 3. Simula o comportamento: Quando o passwordEncoder.encode for chamado, retorna uma senha fake
        when(passwordEncoder.encode("123456")).thenReturn("senha_criptografada");

        // 4. Simula o comportamento: Quando o repository.save for chamado, retorna o usuário com ID
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        // --- ACT (Ação) ---
        UsuarioResponseDTO response = usuarioService.criarUsuario(requestDTO);

        // --- ASSERT (Verificação) ---
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Teste User", response.getNome());
        assertEquals("teste@user.com", response.getEmail());
        assertEquals(List.of("pizza", "sushi"), response.getPreferenciasUsuario());

        // Verifica se o método save() foi chamado exatamente 1 vez
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar usuário com email existente")
    void criarUsuario_ComEmailExistente() {
        // --- ARRANGE (Preparação) ---
        // Simula que o email "teste@user.com" JÁ EXISTE no banco
        when(usuarioRepository.findByEmail("teste@user.com")).thenReturn(Optional.of(usuario));

        // --- ACT & ASSERT (Ação e Verificação) ---
        // Verifica se uma RuntimeException é lançada
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.criarUsuario(requestDTO);
        });

        // Verifica a mensagem da exceção
        assertEquals("Email já cadastrado", exception.getMessage());

        // Verifica se o método save() NUNCA foi chamado
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void atualizarUsuario_ComSucesso() {
        // --- ARRANGE (Preparação) ---
        UsuarioUpdateRequestDTO updateDTO = new UsuarioUpdateRequestDTO();
        updateDTO.setNome("Nome Atualizado");
        updateDTO.setPreferenciasUsuario(List.of("hamburguer"));

        // 1. Simula a busca do usuário no banco
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        // 2. Simula a ação de salvar (retorna o próprio objeto modificado)
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // --- ACT (Ação) ---
        UsuarioUpdateResponseDTO response = usuarioService.atualizarUsuario(1L, updateDTO);

        // --- ASSERT (Verificação) ---
        assertNotNull(response);
        assertEquals("Nome Atualizado", response.getNome());
        assertEquals(List.of("hamburguer"), response.getPreferenciasUsuario());

        // Verifica se o repository.findById e o repository.save foram chamados
        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve buscar os favoritos de um usuário")
    void buscarFavoritos_ComSucesso() {
        // --- ARRANGE (Preparação) ---
        Estabelecimento favorito = new Estabelecimento();
        favorito.setIdEstabelecimento(10L);
        favorito.setNome("Bar do Zé");

        usuario.setFavoritos(Set.of(favorito));

        // Simula a busca do repositório (usando a query customizada)
        when(usuarioRepository.findUserFaviritosById(1L)).thenReturn(Optional.of(usuario));

        // --- ACT (Ação) ---
        Set<Estabelecimento> favoritosResponse = usuarioService.buscarFavoritos(1L);

        // --- ASSERT (Verificação) ---
        assertNotNull(favoritosResponse);
        assertFalse(favoritosResponse.isEmpty());
        assertEquals(1, favoritosResponse.size());
        assertEquals("Bar do Zé", favoritosResponse.iterator().next().getNome());
    }

    @Test
    @DisplayName("Deve favoritar um estabelecimento")
    void favoritarEstabelecimento_ComSucesso() {
        // --- ARRANGE (Preparação) ---
        usuario.setFavoritos(new HashSet<>()); // Começa com lista vazia

        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setIdEstabelecimento(10L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(estabelecimentoService.buscarPorId(10L)).thenReturn(estabelecimento);

        // --- ACT (Ação) ---
        HttpStatus status = usuarioService.favoritarEstabelecimento(1L, 10L);

        // --- ASSERT (Verificação) ---
        assertEquals(HttpStatus.NO_CONTENT, status);
        // Verifica se o estabelecimento foi realmente adicionado ao set de favoritos do usuário
        assertTrue(usuario.getFavoritos().contains(estabelecimento));
        // Verifica se o repository.save foi chamado
        verify(usuarioRepository, times(1)).save(usuario);
    }
}