package com.eti.qualaboa.cupomtest.controllertest;

import com.eti.qualaboa.config.SecurityConfig;
import com.eti.qualaboa.cupom.controller.CupomController;
import com.eti.qualaboa.cupom.dto.CupomDTO;
import com.eti.qualaboa.cupom.service.CupomService;
import com.eti.qualaboa.enums.TipoCupom;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CupomController.class)
@Import(SecurityConfig.class)
public class CupomControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CupomService cupomService;

    private CupomDTO mockCupomDTO;

    @BeforeEach
    void setUp() {
        mockCupomDTO = CupomDTO.builder()
                .idCupom(1L)
                .idEstabelecimento(1L)
                .codigo("NATAL10")
                .descricao("10% Off no Natal")
                .tipo(TipoCupom.DESCONTO)
                .valor(10.0)
                .dataInicio(LocalDateTime.now())
                .dataFim(LocalDateTime.now().plusDays(5))
                .ativo(true)
                .quantidadeTotal(100)
                .quantidadeUsada(0)
                .build();
    }


    @ParameterizedTest
    @MethodSource("endpointsProvider")
    @DisplayName("Deve retornar 401 Unauthorized para todas as rotas sem autenticação")
    void deveRetornar401ParaEndpointsProtegidos(MockHttpServletRequestBuilder requestBuilder) throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized());
    }

    private static Stream<Arguments> endpointsProvider() {
        return Stream.of(
                Arguments.of(get("/cupons")),
                Arguments.of(get("/cupons/1")),
                Arguments.of(post("/cupons").contentType(MediaType.APPLICATION_JSON).content("{}")),
                Arguments.of(put("/cupons/1").contentType(MediaType.APPLICATION_JSON).content("{}")),
                Arguments.of(delete("/cupons/1"))
        );
    }


    @Test
    @DisplayName("GET /cupons - Deve listar cupons com autenticação")
    void listar_ComAutenticacao_DeveRetornarLista() throws Exception {
        when(cupomService.listarTodos()).thenReturn(List.of(mockCupomDTO));

        mockMvc.perform(get("/cupons")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].codigo", is("NATAL10")));
    }

    @Test
    @DisplayName("GET /cupons/{id} - Deve buscar cupom por ID com autenticação")
    void buscarPorId_ComAutenticacao_DeveRetornarCupom() throws Exception {

        when(cupomService.buscarPorId(1L)).thenReturn(mockCupomDTO);


        mockMvc.perform(get("/cupons/1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCupom", is(1)))
                .andExpect(jsonPath("$.descricao", is("10% Off no Natal")));
    }

    @Test
    @DisplayName("POST /cupons - Deve criar cupom com autenticação")
    void criar_ComAutenticacao_DeveRetornarCupomCriado() throws Exception {
        when(cupomService.criarCupom(any(CupomDTO.class))).thenReturn(mockCupomDTO);

        mockMvc.perform(post("/cupons")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCupomDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCupom", is(1)));
    }

    @Test
    @DisplayName("PUT /cupons/{id} - Deve atualizar cupom com autenticação")
    void atualizar_ComAutenticacao_DeveRetornarCupomAtualizado() throws Exception {
        when(cupomService.atualizarCupom(eq(1L), any(CupomDTO.class))).thenReturn(mockCupomDTO);

        mockMvc.perform(put("/cupons/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockCupomDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo", is("NATAL10")));
    }

    @Test
    @DisplayName("DELETE /cupons/{id} - Deve deletar cupom com autenticação")
    void deletar_ComAutenticacao_DeveRetornarNoContent() throws Exception {
        doNothing().when(cupomService).deletarCupom(1L);

        mockMvc.perform(delete("/cupons/1")
                        .with(jwt()))
                .andExpect(status().isNoContent());
    }
}