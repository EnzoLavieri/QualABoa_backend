package com.eti.qualaboa.promocaotest.controllertest;

import com.eti.qualaboa.config.SecurityConfig;
import com.eti.qualaboa.promocao.controller.PromocaoController;
import com.eti.qualaboa.promocao.dto.PromocaoDTO;
import com.eti.qualaboa.promocao.service.PromocaoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

// Importa o 'jwt' para simular a autenticação
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromocaoController.class) // 1. Testa apenas o PromocaoController
@Import(SecurityConfig.class) // 2. Importa a configuração de segurança real
public class PromocaoControllerTest {

    @Autowired
    private MockMvc mockMvc; // 3. Ferramenta para simular requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // 4. Para converter objetos Java em JSON

    @MockBean // 5. Cria um Mock do Serviço (não usa o serviço real)
    private PromocaoService promocaoService;

    private PromocaoDTO mockPromocaoDTO;

    @BeforeEach
    void setUp() {
        // Prepara um DTO de promoção para usar nos testes
        mockPromocaoDTO = PromocaoDTO.builder()
                .idPromocao(1L)
                .idEstabelecimento(1L)
                .idCupom(1L)
                .descricao("Promoção de Teste")
                .desconto(10.0)
                .ativa(true)
                .build();
    }

    // --- Testes de Segurança ---

    @Test
    @DisplayName("Deve falhar ao listar promoções sem token (401 Unauthorized)")
    void deveFalharListarSemAutenticacao() throws Exception {
        // --- ARRANGE (Nenhum) ---

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/promocoes")) // Tenta acessar o endpoint
                .andExpect(status().isUnauthorized()); // Espera um erro 401
    }

    @Test
    @DisplayName("Deve falhar ao criar promoção sem token (401 Unauthorized)")
    void deveFalharCriarSemAutenticacao() throws Exception {
        // --- ARRANGE ---
        String jsonBody = objectMapper.writeValueAsString(mockPromocaoDTO);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/promocoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized()); // Espera um erro 401
    }

    // --- Testes de Funcionalidade (Autenticados) ---

    @Test
    @DisplayName("Deve listar todas as promoções (GET /promocoes)")
    void deveListarTodasPromocoes() throws Exception {
        // --- ARRANGE ---
        // Simula o serviço retornando uma lista com a promoção mockada
        when(promocaoService.listarTodas()).thenReturn(List.of(mockPromocaoDTO));

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/promocoes")
                        .with(jwt())) // 6. Simula um usuário logado (com qualquer role)
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.length()").value(1)) // Espera 1 item na lista
                .andExpect(jsonPath("$[0].descricao").value("Promoção de Teste"));
    }

    @Test
    @DisplayName("Deve buscar promoção por ID (GET /promocoes/{id})")
    void deveBuscarPromocaoPorId() throws Exception {
        // --- ARRANGE ---
        // Simula o serviço retornando a promoção ao buscar por ID 1
        when(promocaoService.buscarPorId(1L)).thenReturn(mockPromocaoDTO);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/promocoes/1")
                        .with(jwt())) // Simula usuário logado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPromocao").value(1L))
                .andExpect(jsonPath("$.descricao").value("Promoção de Teste"));
    }

    @Test
    @DisplayName("Deve criar uma nova promoção (POST /promocoes)")
    void deveCriarPromocao() throws Exception {
        // --- ARRANGE ---
        // Converte o DTO em JSON
        String jsonBody = objectMapper.writeValueAsString(mockPromocaoDTO);

        // Simula o serviço criando e retornando a promoção
        when(promocaoService.criarPromocao(any(PromocaoDTO.class))).thenReturn(mockPromocaoDTO);

        // --- ACT & ASSERT ---
        mockMvc.perform(post("/promocoes")
                        .with(jwt()) // Simula usuário logado
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk()) // O controller retorna 200 OK
                .andExpect(jsonPath("$.idPromocao").value(1L));
    }

    @Test
    @DisplayName("Deve atualizar uma promoção (PUT /promocoes/{id})")
    void deveAtualizarPromocao() throws Exception {
        // --- ARRANGE ---
        // Cria um DTO com dados atualizados
        PromocaoDTO dtoAtualizado = PromocaoDTO.builder()
                .idPromocao(1L)
                .descricao("Promoção Atualizada")
                .desconto(20.0)
                .build();
        String jsonBody = objectMapper.writeValueAsString(dtoAtualizado);

        // Simula o serviço atualizando e retornando o DTO atualizado
        when(promocaoService.atualizarPromocao(eq(1L), any(PromocaoDTO.class)))
                .thenReturn(dtoAtualizado);

        // --- ACT & ASSERT ---
        mockMvc.perform(put("/promocoes/1")
                        .with(jwt()) // Simula usuário logado
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Promoção Atualizada"))
                .andExpect(jsonPath("$.desconto").value(20.0));
    }

    @Test
    @DisplayName("Deve deletar uma promoção (DELETE /promocoes/{id})")
    void deveDeletarPromocao() throws Exception {
        // --- ARRANGE ---
        // Simula o serviço de deleção (não retorna nada)
        doNothing().when(promocaoService).deletarPromocao(1L);

        // --- ACT & ASSERT ---
        mockMvc.perform(delete("/promocoes/1")
                        .with(jwt())) // Simula usuário logado
                .andExpect(status().isNoContent()); // Espera 204 No Content
    }
}