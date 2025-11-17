package com.eti.qualaboa.estabelecimentotest.controllertest;

import com.eti.qualaboa.config.SecurityConfig;
import com.eti.qualaboa.cupom.dto.CupomDTO;
import com.eti.qualaboa.enums.TipoCupom;
import com.eti.qualaboa.estabelecimento.controller.EstabelecimentoController;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoRegisterDTO;
import com.eti.qualaboa.estabelecimento.dto.EstabelecimentoResponseDTO;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.service.EstabelecimentoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstabelecimentoController.class)
@Import(SecurityConfig.class)
public class EstabelecimentoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EstabelecimentoService estabelecimentoService;

    private EstabelecimentoRegisterDTO registerDTO;
    private EstabelecimentoResponseDTO responseDTO;
    private EstabelecimentoDTO estabelecimentoDTO;
    private Estabelecimento estabelecimento;
    private CupomDTO cupomDTO;

    @BeforeEach
    void setUp() {
        registerDTO = new EstabelecimentoRegisterDTO();
        registerDTO.setNome("Bar do Teste");
        registerDTO.setEmail("teste@bar.com");
        registerDTO.setSenha("123456");
        registerDTO.setIdRole(3L);

        responseDTO = new EstabelecimentoResponseDTO(
                1L, "Bar do Teste", "teste@bar.com", "bar",
                "Descrição", "123456", true, "place123", -23.0, -51.0,
                "Rua Teste, 123", null, 5.0, List.of("wifi")
        );

        estabelecimentoDTO = EstabelecimentoDTO.builder()
                .idEstabelecimento(1L)
                .nome("Bar do Teste")
                .placeId("place123")
                .build();

        estabelecimento = Estabelecimento.builder()
                .idEstabelecimento(1L)
                .nome("Bar do Teste")
                .build();

        cupomDTO = CupomDTO.builder()
                .idCupom(1L)
                .codigo("TESTE10")
                .idEstabelecimento(1L)
                .tipo(TipoCupom.DESCONTO)
                .build();
    }


    @Test
    @DisplayName("POST /estabelecimentos - Deve permitir acesso público")
    void criar_DevePermitirAcessoPublico() throws Exception {
        when(estabelecimentoService.criar(any(EstabelecimentoRegisterDTO.class))).thenReturn(responseDTO);

        mockMvc.perform(post("/estabelecimentos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerDTO)))
                .andExpect(status().isOk()) // Espera 200 OK (como está no controller)
                .andExpect(jsonPath("$.nome", is("Bar do Teste")))
                .andExpect(jsonPath("$.email", is("teste@bar.com")));
    }

    @Test
    @DisplayName("GET /estabelecimentos - Deve retornar 401 sem autenticação")
    void listarTodos_SemAutenticacao_DeveRetornar401() throws Exception {
        mockMvc.perform(get("/estabelecimentos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /{id} - Deve retornar 401 sem autenticação")
    void buscarPorId_SemAutenticacao_DeveRetornar401() throws Exception {
        mockMvc.perform(get("/estabelecimentos/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /cupons/{id} - Deve retornar 401 sem autenticação")
    void listarCupons_SemAutenticacao_DeveRetornar401() throws Exception {
        mockMvc.perform(get("/estabelecimentos/cupons/1"))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("GET /estabelecimentos - Deve retornar lista com autenticação")
    void listarTodos_ComAutenticacao_DeveRetornarLista() throws Exception {
        when(estabelecimentoService.listarTodos()).thenReturn(List.of(estabelecimentoDTO));

        mockMvc.perform(get("/estabelecimentos")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome", is("Bar do Teste")));
    }

    @Test
    @DisplayName("GET /{id} - Deve retornar estabelecimento com autenticação")
    void buscarPorId_ComAutenticacao_DeveRetornarEstabelecimento() throws Exception {
        when(estabelecimentoService.buscarPorId(1L)).thenReturn(estabelecimento);

        mockMvc.perform(get("/estabelecimentos/1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEstabelecimento", is(1)));
    }

    @Test
    @DisplayName("PUT /{id} - Deve atualizar estabelecimento com autenticação")
    void atualizar_ComAutenticacao_DeveRetornarDTO() throws Exception {
        when(estabelecimentoService.atualizar(eq(1L), any(Estabelecimento.class))).thenReturn(estabelecimentoDTO);

        mockMvc.perform(put("/estabelecimentos/1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(estabelecimento)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idEstabelecimento", is(1)));
    }

    @Test
    @DisplayName("DELETE /{id} - Deve deletar estabelecimento com autenticação")
    void deletar_ComAutenticacao_DeveRetornarNoContent() throws Exception {
        doNothing().when(estabelecimentoService).deletar(1L);

        mockMvc.perform(delete("/estabelecimentos/1")
                        .with(jwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("PUT /{id}/link-place - Deve vincular place com autenticação")
    void vincularPlace_ComAutenticacao_DeveRetornarDTO() throws Exception {
        String placeId = "place_teste_123";
        when(estabelecimentoService.vincularComPlace(1L, placeId)).thenReturn(estabelecimentoDTO);

        mockMvc.perform(put("/estabelecimentos/1/link-place")
                        .param("placeId", placeId)
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.placeId", is("place123")));
    }

    @Test
    @DisplayName("GET /cupons/{id} - Deve listar cupons com autenticação")
    void listarCupons_ComAutenticacao_DeveRetornarListaDeCupons() throws Exception {
        when(estabelecimentoService.listarCuponsPorEstabelecimento(1L)).thenReturn(List.of(cupomDTO));

        mockMvc.perform(get("/estabelecimentos/cupons/1")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo", is("TESTE10")));
    }
}
