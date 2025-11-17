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


@WebMvcTest(TokenController.class)
@Import(SecurityConfig.class)
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @MockBean
    private UsuarioRepository usuarioRepository;

    @MockBean
    private EstabelecimentoRepository estabelecimentoRepository;

    private Usuario mockUsuario;
    private Estabelecimento mockEstabelecimento;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setId(1L);
        userRole.setNome("USER");

        mockUsuario = new Usuario();
        mockUsuario.setId(1L);
        mockUsuario.setEmail("user@teste.com");
        mockUsuario.setSenha(passwordEncoder.encode("123456"));
        mockUsuario.setRoles(Set.of(userRole));

        Role estRole = new Role();
        estRole.setId(3L);
        estRole.setNome("ESTABELECIMENTO");

        mockEstabelecimento = new Estabelecimento();
        mockEstabelecimento.setIdEstabelecimento(10L);
        mockEstabelecimento.setEmail("bar@teste.com");
        mockEstabelecimento.setSenha(passwordEncoder.encode("senhaforte"));
        mockEstabelecimento.setRoles(Set.of(estRole));
    }


    @Test
    @DisplayName("Login de Usuário com credenciais corretas deve retornar Token")
    void loginUsuario_ComCredenciaisCorretas_DeveRetornarToken() throws Exception {

        when(usuarioRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(mockUsuario));

        LoginRequest loginRequest = new LoginRequest("user@teste.com", "123456");


        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    @DisplayName("Login de Usuário com senha incorreta deve retornar 400 BadCredentials")
    void loginUsuario_ComSenhaIncorreta_DeveRetornar400() throws Exception {

        when(usuarioRepository.findByEmail("user@teste.com")).thenReturn(Optional.of(mockUsuario));
        LoginRequest loginRequest = new LoginRequest("user@teste.com", "senhaerrada");


        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login de Usuário com email inexistente deve retornar 400 BadCredentials")
    void loginUsuario_ComEmailInexistente_DeveRetornar400() throws Exception {

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        LoginRequest loginRequest = new LoginRequest("emailnaoexiste@teste.com", "123456");

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }



    @Test
    @DisplayName("Login de Estabelecimento com credenciais corretas deve retornar Token")
    void loginEstabelecimento_ComCredenciaisCorretas_DeveRetornarToken() throws Exception {
        when(estabelecimentoRepository.findByEmail("bar@teste.com")).thenReturn(Optional.of(mockEstabelecimento));
        LoginRequest loginRequest = new LoginRequest("bar@teste.com", "senhaforte");

        mockMvc.perform(post("/auth/login/estabelecimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.userID").value(10L));
    }

    @Test
    @DisplayName("Login de Estabelecimento com senha incorreta deve retornar 400 BadCredentials")
    void loginEstabelecimento_ComSenhaIncorreta_DeveRetornar400() throws Exception {

        when(estabelecimentoRepository.findByEmail("bar@teste.com")).thenReturn(Optional.of(mockEstabelecimento));
        LoginRequest loginRequest = new LoginRequest("bar@teste.com", "senhafraca");

        mockMvc.perform(post("/auth/login/estabelecimento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}