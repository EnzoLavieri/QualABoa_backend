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

@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/usuarios - Deve criar um novo usuário com sucesso (público)")
    void criarUsuario_DeveRetornarCreated() throws Exception {
        UsuarioRequestDTO requestDTO = new UsuarioRequestDTO();
        requestDTO.setNome("Test User");
        requestDTO.setEmail("test@user.com");
        requestDTO.setSenha("senha123");
        requestDTO.setIdRole(1L); // ID 1 = USER

        UsuarioResponseDTO responseDTO = new UsuarioResponseDTO(
                1L, "Test User", "test@user.com", null, List.of()
        );

        when(usuarioService.criarUsuario(any(UsuarioRequestDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Test User"))
                .andExpect(jsonPath("$.email").value("test@user.com"));
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Deve falhar sem autenticação (401)")
    void buscarPorId_SemAutenticacao_DeveRetornarUnauthorized() throws Exception {

        mockMvc.perform(get("/api/usuarios/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/usuarios/{id} - Deve retornar usuário com autenticação (USER)")
    void buscarPorId_ComAutenticacao_DeveRetornarOk() throws Exception {
        Usuario usuarioMock = new Usuario();
        usuarioMock.setId(1L);
        usuarioMock.setNome("Test User");
        usuarioMock.setEmail("test@user.com");
        usuarioMock.setSexo(Sexo.FEMININO);

        when(usuarioService.findUserById(1L)).thenReturn(usuarioMock);

        mockMvc.perform(get("/api/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isOk()) // Verifica se o status é 200 OK
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Test User"));
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Deve falhar para ROLE_USER (403)")
    void deletarUsuario_ComRoleUser_DeveRetornarForbidden() throws Exception {
        mockMvc.perform(delete("/api/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_USER"))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/usuarios/{id} - Deve funcionar para ROLE_ADMIN (204)")
    void deletarUsuario_ComRoleAdmin_DeveRetornarNoContent() throws Exception {
        when(usuarioService.deletarUsuario(1L)).thenReturn(HttpStatus.NO_CONTENT);

        mockMvc.perform(delete("/api/usuarios/1")
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_ADMIN"))))
                .andExpect(status().isNoContent());
    }
}