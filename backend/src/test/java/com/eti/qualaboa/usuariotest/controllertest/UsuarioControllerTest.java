package com.eti.qualaboa.usuariotest.controllertest;

import com.eti.qualaboa.config.SecurityConfig;
import com.eti.qualaboa.enums.Sexo;
import com.eti.qualaboa.usuario.controller.UsuarioController;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import com.eti.qualaboa.usuario.dto.UsuarioRequestDTO;
import com.eti.qualaboa.usuario.dto.UsuarioResponseDTO;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsuarioController.class) // 1. Indica que é um teste focado na camada Web (Controller)
@Import(SecurityConfig.class) // 2. Importa sua configuração de segurança real para testar as regras
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc; // 3. Objeto para simular requisições HTTP

    @MockBean // 4. Cria um MOCK (dublê) do UsuarioService
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper; // 5. Converte objetos Java para JSON

    @Test
    @DisplayName("POST /api/usuarios - Deve criar um novo usuário com sucesso (público)")
    void criarUsuario_DeveRetornarCreated() throws Exception {
        // --- ARRANGE (Preparação) ---
        // 1. O que o usuário vai enviar no corpo da requisição (Request)
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO();
        requestDTO.setNome("Test User");
        requestDTO.setEmail("test@user.com");
        requestDTO.setSenha("senha123");
        requestDTO.setIdRole(1L); // ID 1 = USER

        // 2. O que o UsuarioService (mockado) deve retornar
        UsuarioResponseDTO responseDTO = new UsuarioResponseDTO(
                1L, "Test User", "test@user.com", null, List.of()
        );

        // 3. Configurar o mock: "Quando o service.criarUsuario for chamado, retorne o responseDTO"
        when(usuarioService.criarUsuario(any(UsuarioRequestDTO.class))).thenReturn(responseDTO);

        // --- ACT & ASSERT (Ação e Verificação) ---
        mockMvc.perform(post("/api/usuarios") // Simula um POST para /api/usuarios
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO))) // Converte o DTO em JSON
                .andExpect(status().isCreated()) // Verifica se o status é 201 Created
                .andExpect(jsonPath("$.id").value(1L)) // Verifica o JSON de resposta
                .andExpect(jsonPath("$.nome").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@user.com"));
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Deve falhar sem autenticação (401)")
    void buscarPorId_SemAutenticacao_DeveRetornarUnauthorized() throws Exception {
        // --- ARRANGE (Nenhum) ---

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/api/usuarios/1") // Simula um GET
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // 401 Unauthorized (Regra do SecurityConfig)
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Deve retornar usuário com autenticação (USER)")
    void buscarPorId_ComAutenticacao_DeveRetornarOk() throws Exception {
        // --- ARRANGE ---
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setNome("Test User");
        usuarioMock.setEmail("test@user.com");
        usuarioMock.setSexo(Sexo.FEMININO);

        // Configura o mock: "Quando o service.findUserById(1L) for chamado, retorne o usuarioMock"
        when(usuarioService.findUserById(1L)).thenReturn(usuarioMock);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/api/usuarios/1")
                        // Simula um token JWT válido com a ROLE_USER
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isOk()) // Verifica se o status é 200 OK
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Test User"))
                .andExpect(jsonPath("$.sexo").value("FEMININO"));
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Deve falhar para ROLE_USER (403)")
    void deletarUsuario_ComRoleUser_DeveRetornarForbidden() throws Exception {
        // --- ARRANGE (Nenhum) ---
        // A segurança deve bloquear a requisição antes mesmo de chamar o service.

        // --- ACT & ASSERT ---
        mockMvc.perform(delete("/api/usuarios/1")
                        // Simula um token JWT válido com a ROLE_USER
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isForbidden()); // 403 Forbidden (Regra do @PreAuthorize)
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Deve funcionar para ROLE_ADMIN (204)")
    void deletarUsuario_ComRoleAdmin_DeveRetornarNoContent() throws Exception {
        // --- ARRANGE ---
        // O service retorna HttpStatus, então mockamos isso
        when(usuarioService.deletarUsuario(1L)).thenReturn(HttpStatus.NO_CONTENT);

        // --- ACT & ASSERT ---
        mockMvc.perform(delete("/api/usuarios/1")
                        // Simula um token JWT válido com a ROLE_ADMIN
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_ADMIN"))))
                .andExpect(status().isNoContent()); // 204 No Content
    }
}