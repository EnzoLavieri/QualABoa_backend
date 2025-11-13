package com.eti.qualaboa.configtest.controllertest;

import com.eti.qualaboa.config.SecurityConfig;
import com.eti.qualaboa.config.controller.TokenController;
import com.eti.qualaboa.config.dto.LoginRequest;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.Set;

import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Teste unitário para o TokenController.
 * Este teste carrega a SecurityConfig real para usar o JwtEncoder e o PasswordEncoder reais,
 * mas "mocka" os repositórios (UsuarioRepository, EstabelecimentoRepository)
 * para simular as buscas no banco de dados.
 */
@WebMvcTest(TokenController.class)
@Import(SecurityConfig.class) // Importa a configuração real de segurança
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    // Usamos o PasswordEncoder real injetado pela SecurityConfig para criar senhas
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // --- Mocks para as dependências de banco de dados ---
    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private EstabelecimentoRepository estabelecimentoRepository;

    // --- Entidades Mock para os testes ---
    private Usuario mockUsuario;
    private Estabelecimento mockEstabelecimento;

    @BeforeEach
    void setUp() {
        // 1. Cria uma Role "USER"
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setNome("USER");

        // 2. Cria um Usuário mockado com senha criptografada
        mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setEmail("user@teste.com");
        // Usa o encoder real para criar um hash da senha "123456"
        mockUsuario.setSenha(passwordEncoder.encode("123456"));
        mockUsuario.setRoles(Set.of(userRole));

        // 3. Cria uma Role "ESTABELECIMENTO"
        Role estRole = new Role();
        estRole.setId(3L);
        estRole.setNome("ESTABELECIMENTO");

        // 4. Cria um Estabelecimento mockado
        mockEstabelecimento = new Estabelecimento();
        mockEstabelecimento.setIdEstabelecimento(10L);
        mockEstabelecimento.setEmail("bar@teste.com");
        mockEstabelecimento.setSenha(passwordEncoder.encode("senhaforte"));
        mockEstabelecimento.setRoles(Set.of(estRole));
    }

    // --- Testes do Endpoint /auth/login (Usuário) ---

    @Test
    @DisplayName("Login de Usuário com credenciais corretas deve retornar Token")
    void loginUsuario_ComCredenciaisCorretas_DeveRetornarToken() throws Exception {
        // ARRANGE
        // Simula que o repositório encontrou o usuário pelo email
        when(usuarioRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(mockUsuario));

        LoginRequest loginRequest = new LoginRequest("user@teste.com", "123456");

        // ACT & ASSERT
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.expiresIn").value(300L))
                .andExpect(jsonPath("$.userID").value(1L));
    }

    @Test
    @DisplayName("Login de Usuário com senha incorreta deve retornar 400 BadCredentials")
    void loginUsuario_ComSenhaIncorreta_DeveRetornar400() throws Exception {
        // ARRANGE
        // O usuário é encontrado
        when(usuarioRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(mockUsuario));
        // Mas a senha enviada está errada
        LoginRequest loginRequest = new LoginRequest("user@teste.com", "senhaerrada");

        // ACT & ASSERT
        // O bCryptPasswordEncoder real (da SecurityConfig) fará a verificação e falhará
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest()); // O controller lança BadCredentialsException
    }

    @Test
    @DisplayName("Login de Usuário com email inexistente deve retornar 400 BadCredentials")
    void loginUsuario_ComEmailInexistente_DeveRetornar400() throws Exception {
        // ARRANGE
        // Simula que o repositório NÃO encontrou o usuário
        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        LoginRequest loginRequest = new LoginRequest("emailnaoexiste@teste.com", "123456");

        // ACT & ASSERT
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // --- Testes do Endpoint /auth/login/estabelecimento ---

    @Test
    @DisplayName("Login de Estabelecimento com credenciais corretas deve retornar Token")
    void loginEstabelecimento_ComCredenciaisCorretas_DeveRetornarToken() throws Exception {
        // ARRANGE
        when(estabelecimentoRepository.findByEmail("bar@teste.com")).thenReturn(Optional.of(mockEstabelecimento));
        LoginRequest loginRequest = new LoginRequest("bar@teste.com", "senhaforte");

        // ACT & ASSERT
        mockMvc.perform(post("/auth/login/estabelecimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.userID").value(10L)); // ID do estabelecimento
    }

    @Test
    @DisplayName("Login de Estabelecimento com senha incorreta deve retornar 400 BadCredentials")
    void loginEstabelecimento_ComSenhaIncorreta_DeveRetornar400() throws Exception {
        // ARRANGE
        when(estabelecimentoRepository.findByEmail("bar@teste.com")).thenReturn(Optional.of(mockEstabelecimento));
        LoginRequest loginRequest = new LoginRequest("bar@teste.com", "senhafraca");

        // ACT & ASSERT
        mockMvc.perform(post("/auth/login/estabelecimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }
}