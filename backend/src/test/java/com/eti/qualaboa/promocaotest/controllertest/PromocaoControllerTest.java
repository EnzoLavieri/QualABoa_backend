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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PromocaoController.class)
@Import(SecurityConfig.class)
public class PromocaoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PromocaoService promocaoService;

    private PromocaoDTO mockPromocaoDTO;

    @BeforeEach
    void setUp() {
        mockPromocaoDTO = PromocaoDTO.builder()
                .idPromocao(1L)
                .idEstabelecimento(1L)
                .idCupom(1L)
                .descricao("Promoção de Teste")
                .desconto(10.0)
                .ativa(true)
                .build();
    }


    @Test
    @DisplayName("Deve falhar ao listar promoções sem token (401 Unauthorized)")
    void deveFalharListarSemAutenticacao() throws Exception {

        mockMvc.perform(get("/promocoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve falhar ao criar promoção sem token (401 Unauthorized)")
    void deveFalharCriarSemAutenticacao() throws Exception {
        String jsonBody = objectMapper.writeValueAsString(mockPromocaoDTO);

        mockMvc.perform(post("/promocoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("Deve listar todas as promoções (GET /promocoes)")
    void deveListarTodasPromocoes() throws Exception {

        when(promocaoService.listarTodas()).thenReturn(List.of(mockPromocaoDTO));

        mockMvc.perform(get("/promocoes")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].descricao").value("Promoção de Teste"));
    }

    @Test
    @DisplayName("Deve buscar promoção por ID (GET /promocoes/{id})")
    void deveBuscarPromocaoPorId() throws Exception {

        when(promocaoService.buscarPorId(1L)).thenReturn(mockPromocaoDTO);

        mockMvc.perform(get("/promocoes/1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPromocao").value(1L))
                .andExpect(jsonPath("$.descricao").value("Promoção de Teste"));
    }

    @Test
    @DisplayName("Deve criar uma nova promoção (POST /promocoes)")
    void deveCriarPromocao() throws Exception {

        String jsonBody = objectMapper.writeValueAsString(mockPromocaoDTO);

        when(promocaoService.criarPromocao(any(PromocaoDTO.class))).thenReturn(mockPromocaoDTO);

        mockMvc.perform(post("/promocoes")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idPromocao").value(1L));
    }

    @Test
    @DisplayName("Deve atualizar uma promoção (PUT /promocoes/{id})")
    void deveAtualizarPromocao() throws Exception {

        PromocaoDTO dtoAtualizado = PromocaoDTO.builder()
                .idPromocao(1L)
                .descricao("Promoção Atualizada")
                .desconto(20.0)
                .build();
        String jsonBody = objectMapper.writeValueAsString(dtoAtualizado);

        when(promocaoService.atualizarPromocao(eq(1L), any(PromocaoDTO.class)))
                .thenReturn(dtoAtualizado);

        mockMvc.perform(put("/promocoes/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descricao").value("Promoção Atualizada"))
                .andExpect(jsonPath("$.desconto").value(20.0));
    }

    @Test
    @DisplayName("Deve deletar uma promoção (DELETE /promocoes/{id})")
    void deveDeletarPromocao() throws Exception {

        doNothing().when(promocaoService).deletarPromocao(1L);

        mockMvc.perform(delete("/promocoes/1")
                        .with(jwt()))
                .andExpect(status().isNoContent());
    }
}