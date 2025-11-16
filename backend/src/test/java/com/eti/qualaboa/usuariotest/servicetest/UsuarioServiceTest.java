package com.eti.qualaboa.usuariotest.servicetest;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import com.eti.qualaboa.metricas.service.MetricasService;
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

@ExtendWith(MockitoExtension.class)
public class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private EstabelecimentoService estabelecimentoService;

    @Mock
    private MetricasService metricasService;

    @InjectMocks
    private UsuarioService usuarioService;


    private UsuarioRequestDTO requestDTO;
    private Usuario usuario;
    private Role roleUser;

    @BeforeEach
    void setUp() {

        requestDTO = new UsuarioRequestDTO();
        requestDTO.setNome("Teste User");
        requestDTO.setEmail("teste@user.com");
        requestDTO.setSenha("123456");
        requestDTO.setIdRole(1L); // 1L = USER
        requestDTO.setPreferenciasUsuario(List.of("pizza", "sushi"));

        roleUser = new Role();
        roleUser.setId(1L);
        roleUser.setNome("USER");

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

        when(usuarioRepository.findByEmail("teste@user.com")).thenReturn(Optional.empty());

        when(roleRepository.findByNome("USER")).thenReturn(Optional.of(roleUser));

        when(passwordEncoder.encode("123456")).thenReturn("senha_criptografada");

        when(usuarioRepository.save(any(Usuario.class))).thenReturn(usuario);

        UsuarioResponseDTO response = usuarioService.criarUsuario(requestDTO);

        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals("Teste User", response.getNome());
        assertEquals("teste@user.com", response.getEmail());
        assertEquals(List.of("pizza", "sushi"), response.getPreferenciasUsuario());


        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar criar usuário com email existente")
    void criarUsuario_ComEmailExistente() {

        when(usuarioRepository.findByEmail("teste@user.com")).thenReturn(Optional.of(usuario));


        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            usuarioService.criarUsuario(requestDTO);
        });

        assertEquals("Email já cadastrado", exception.getMessage());

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve atualizar um usuário com sucesso")
    void atualizarUsuario_ComSucesso() {
        UsuarioUpdateRequestDTO updateDTO = new UsuarioUpdateRequestDTO();
        updateDTO.setNome("Nome Atualizado");
        updateDTO.setPreferenciasUsuario(List.of("hamburguer"));

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UsuarioUpdateResponseDTO response = usuarioService.atualizarUsuario(1L, updateDTO);

        assertNotNull(response);
        assertEquals("Nome Atualizado", response.getNome());
        assertEquals(List.of("hamburguer"), response.getPreferenciasUsuario());

        verify(usuarioRepository, times(1)).findById(1L);
        verify(usuarioRepository, times(1)).save(any(Usuario.class));
    }

    @Test
    @DisplayName("Deve buscar os favoritos de um usuário")
    void buscarFavoritos_ComSucesso() {
        Estabelecimento favorito = new Estabelecimento();
        favorito.setIdEstabelecimento(10L);
        favorito.setNome("Bar do Zé");

        usuario.setFavoritos(Set.of(favorito));

        when(usuarioRepository.findUserFaviritosById(1L)).thenReturn(Optional.of(usuario));

        Set<Estabelecimento> favoritosResponse = usuarioService.buscarFavoritos(1L);

        assertNotNull(favoritosResponse);
        assertFalse(favoritosResponse.isEmpty());
        assertEquals(1, favoritosResponse.size());
        assertEquals("Bar do Zé", favoritosResponse.iterator().next().getNome());
    }

    @Test
    @DisplayName("Deve favoritar um estabelecimento")
    void favoritarEstabelecimento_ComSucesso() {
        usuario.setFavoritos(new HashSet<>());

        Estabelecimento estabelecimento = new Estabelecimento();
        estabelecimento.setIdEstabelecimento(10L);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        doNothing().when(metricasService).registrarFavorito(10L);

        HttpStatus status = usuarioService.favoritarEstabelecimento(1L, 10L);

        assertEquals(HttpStatus.NO_CONTENT, status);
        assertEquals(1, usuario.getFavoritos().size());


        verify(usuarioRepository, times(1)).save(usuario);

        verify(metricasService, times(1)).registrarFavorito(10L);
    }
}