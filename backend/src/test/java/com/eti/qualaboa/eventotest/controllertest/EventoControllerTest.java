package com.eti.qualaboa.eventotest.controllertest;

import com.eti.qualaboa.config.SecurityConfig; // [CORREÇÃO] Importa sua config
import com.eti.qualaboa.evento.controller.EventoController;
import com.eti.qualaboa.evento.dto.EventoDTO;
import com.eti.qualaboa.evento.service.EventoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import; // [CORREÇÃO] Import necessário
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
// Import estático necessário para simular o JWT
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Teste unitário para o EventoController.
 * Foca em testar a camada Web (endpoints e segurança) em isolamento,
 * "mockando" a camada de serviço (EventoService).
 */
@WebMvcTest(EventoController.class)
@Import(SecurityConfig.class) // [CORREÇÃO] Carrega sua config de segurança (para desabilitar o CSRF)
public class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc; // Ferramenta para simular requisições HTTP

    @Autowired
    private ObjectMapper objectMapper; // Para converter objetos em JSON

    @MockBean // Cria um mock do EventoService e o injeta no contexto
    private EventoService eventoService;

    private EventoDTO eventoDTO;

    @BeforeEach
    void setUp() {
        // Cria um DTO de evento padrão para usar nos testes
        eventoDTO = EventoDTO.builder()
                .idEvento(1L)
                .idEstabelecimento(1L)
                .titulo("Evento Teste")
                .descricao("Descrição do evento")
                .build();
    }

    // --- Testes de Segurança (Não Autenticado) ---

    // Este teste é parametrizado para rodar uma vez para cada endpoint
    // e verificar se todos estão protegidos contra acesso não autenticado.
    @ParameterizedTest
    @MethodSource("endpointsProvider")
    @DisplayName("Deve retornar 401 Unauthorized para todas as rotas sem autenticação")
    void deveRetornar401ParaEndpointsProtegidos(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized()); // Agora deve esperar 401 (e não 403)
    }

    // Provedor de endpoints para o teste parametrizado acima
    private static Stream<Arguments> endpointsProvider() {
        return Stream.of(
                Arguments.of(get("/eventos")),
                Arguments.of(get("/eventos/1")),
                Arguments.of(post("/eventos").contentType(MediaType.APPLICATION_JSON).content("{}")),
                Arguments.of(put("/eventos/1").contentType(MediaType.APPLICATION_JSON).content("{}")),
                Arguments.of(delete("/eventos/1"))
        );
    }

    // --- Testes de Funcionalidade (Autenticado) ---

    @Test
    @DisplayName("GET /eventos - Deve listar eventos com autenticação")
    void listar_ComAutenticacao_DeveRetornarListaDeEventos() throws Exception {
        // ARRANGE
        when(eventoService.listarTodos()).thenReturn(List.of(eventoDTO));

        // ACT & ASSERT
        mockMvc.perform(get("/eventos")
                        .with(jwt())) // Simula um token JWT válido
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].titulo", is("Evento Teste")));
    }

    @Test
    @DisplayName("GET /eventos/{id} - Deve buscar evento por ID com autenticação")
    void buscar_ComAutenticacao_DeveRetornarEvento() throws Exception {
        // ARRANGE
        when(eventoService.buscarPorId(1L)).thenReturn(eventoDTO);

        // ACT & ASSERT
        mockMvc.perform(get("/eventos/1")
                        .with(jwt())) // Simula um token JWT válido
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEvento", is(1)))
                .andExpect(jsonPath("$.titulo", is("Evento Teste")));
    }

    @Test
    @DisplayName("POST /eventos - Deve criar evento com autenticação")
    void criar_ComAutenticacao_DeveRetornarEventoCriado() throws Exception {
        // ARRANGE
        when(eventoService.criarEvento(any(EventoDTO.class))).thenReturn(eventoDTO);

        // ACT & ASSERT
        mockMvc.perform(post("/eventos")
                        .with(jwt()) // Simula um token JWT válido
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventoDTO)))
                .andExpect(status().isOk()) // Seu controller retorna 200 OK
                .andExpect(jsonPath("$.idEvento", is(1)));
    }

    @Test
    @DisplayName("PUT /eventos/{id} - Deve atualizar evento com autenticação")
    void atualizar_ComAutenticacao_DeveRetornarEventoAtualizado() throws Exception {
        // ARRANGE
        when(eventoService.atualizarEvento(eq(1L), any(EventoDTO.class))).thenReturn(eventoDTO);

        // ACT & ASSERT
        mockMvc.perform(put("/eventos/1")
                        .with(jwt()) // Simula um token JWT válido
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(eventoDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo", is("Evento Teste")));
    }

    @Test
    @DisplayName("DELETE /eventos/{id} - Deve deletar evento com autenticação")
    void deletar_ComAutenticacao_DeveRetornarNoContent() throws Exception {
        // ARRANGE
        doNothing().when(eventoService).deletarEvento(1L);

        // ACT & ASSERT
        mockMvc.perform(delete("/eventos/1")
                        .with(jwt())) // Simula um token JWT válido
                .andExpect(status().isNoContent()); // Espera 204 No Content
    }
}