package com.eti.qualaboa.configtest;

import com.eti.qualaboa.config.SecurityConfig;
import com.eti.qualaboa.cupom.service.CupomService;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import com.eti.qualaboa.evento.service.EventoService;
import com.eti.qualaboa.map.service.MapService;
import com.eti.qualaboa.promocao.service.PromocaoService;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.dto.UsuarioRequestDTO;
import com.eti.qualaboa.usuario.dto.UsuarioResponseDTO;
import com.eti.qualaboa.usuario.repository.RoleRepository;
import com.eti.qualaboa.usuario.repository.UsuarioRepository;
import com.eti.qualaboa.usuario.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste para a SecurityConfig.
 * Carrega todos os controllers (@WebMvcTest sem args) e a SecurityConfig real (@Import).
 * Mocka toda a camada de serviço/repositório para isolar o teste na segurança da web.
 */
@WebMvcTest
@Import(SecurityConfig.class)
public class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // --- Mocks de todas as dependências de serviço/repositório ---
    // (Necessários porque @WebMvcTest não carrega a camada de serviço)
    @MockBean
    private UsuarioService usuarioService;
    @MockBean
    private EstabelecimentoService estabelecimentoService;
    @MockBean
    private EventoService eventoService;
    @MockBean
    private PromocaoService promocaoService;
    @MockBean
    private CupomService cupomService;
    @MockBean
    private MapService mapService;
    @MockBean
    private UsuarioRepository usuarioRepository;
    @MockBean
    private EstabelecimentoRepository estabelecimentoRepository;
    @MockBean
    private RoleRepository roleRepository;
    // --- Fim dos Mocks ---

    @Test
    @DisplayName("Endpoint Público (POST /api/usuarios) deve permitir acesso sem token")
    void endpointPublico_POSTUsuarios_DevePermitirAcesso() throws Exception {
        // ARRANGE
        // Prepara a requisição
        UsuarioRequestDTO request = new UsuarioRequestDTO();
        request.setEmail("publico@teste.com");
        request.setNome("Usuario Publico");
        request.setSenha("123456");
        request.setIdRole(1L);

        // Prepara a resposta mockada do serviço
        UsuarioResponseDTO response = new UsuarioResponseDTO(1L, "Usuario Publico", "publico@teste.com", null, null);
        when(usuarioService.criarUsuario(any(UsuarioRequestDTO.class))).thenReturn(response);

        // ACT & ASSERT
        // Executa a chamada SEM token (.with(jwt()))
        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated()); // Espera 201 Created (como no controller)
    }

    @Test
    @DisplayName("Endpoint Protegido (GET /api/usuarios/1) deve retornar 401 Unauthorized sem token")
    void endpointProtegido_GETUsuario_SemToken_DeveRetornar401() throws Exception {
        // ACT & ASSERT
        // Executa a chamada SEM token
        mockMvc.perform(get("/api/usuarios/1"))
                .andExpect(status().isUnauthorized()); // Espera 401
    }

    @Test
    @DisplayName("Endpoint Protegido (GET /api/usuarios/1) deve retornar 200 OK com token")
    void endpointProtegido_GETUsuario_ComToken_DeveRetornar200() throws Exception {
        // ARRANGE
        // Prepara a resposta mockada do serviço
        Usuario mockUser = new Usuario();
        mockUser.setId(1L);
        mockUser.setNome("Test User");
        mockUser.setEmail("test@user.com");
        when(usuarioService.findUserById(1L)).thenReturn(mockUser);

        // ACT & ASSERT
        // Executa a chamada COM token
        mockMvc.perform(get("/api/usuarios/1")
                        .with(jwt())) // Simula um token JWT válido (qualquer role)
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.nome", is("Test User")));
    }

    @Test
    @DisplayName("Endpoint Admin (DELETE /api/usuarios/1) deve retornar 403 Forbidden para role USER")
    void endpointAdmin_DELETEUsuario_ComRoleUser_DeveRetornar403() throws Exception {
        // ACT & ASSERT
        // Executa a chamada COM token de USER
        mockMvc.perform(delete("/api/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_USER")))) // Token com role USER
                .andExpect(status().isForbidden()); // Espera 403 Forbidden
    }

    @Test
    @DisplayName("Endpoint Admin (DELETE /api/usuarios/1) deve retornar 204 No Content para role ADMIN")
    void endpointAdmin_DELETEUsuario_ComRoleAdmin_DeveRetornar204() throws Exception {
        // ARRANGE
        // Simula a resposta do serviço
        when(usuarioService.deletarUsuario(1L)).thenReturn(HttpStatus.NO_CONTENT);

        // ACT & ASSERT
        // Executa a chamada COM token de ADMIN
        mockMvc.perform(delete("/api/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_ADMIN")))) // Token com role ADMIN
                .andExpect(status().isNoContent()); // Espera 204 No Content
    }
}