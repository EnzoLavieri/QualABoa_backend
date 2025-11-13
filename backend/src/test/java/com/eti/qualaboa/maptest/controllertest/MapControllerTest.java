package com.eti.qualaboa.maptest.controllertest;

import com.eti.qualaboa.config.SecurityConfig;
import com.eti.qualaboa.map.controller.MapController;
import com.eti.qualaboa.map.dto.PinDTO;
import com.eti.qualaboa.map.service.MapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

// Importa o 'jwt' para simular a autenticação
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MapController.class) // 1. Testa apenas o MapController
@Import(SecurityConfig.class) // 2. Importa a configuração de segurança real
public class MapControllerTest {

    @Autowired
    private MockMvc mockMvc; // 3. Ferramenta para simular requisições HTTP

    @MockBean // 4. Cria um Mock do MapService
    private MapService mapService;

    private PinDTO mockPin;
    private Map<String, Object> mockPlaceDetails;

    @BeforeEach
    void setUp() {
        // Prepara um DTO de Pin para usar nos testes
        mockPin = PinDTO.builder()
                .id(1L)
                .placeId("place123")
                .nome("Bar do Mock")
                .lat(-23.427)
                .lng(-51.938)
                .isPartner(true)
                .build();

        // Prepara um mapa de detalhes do local
        mockPlaceDetails = Map.of(
                "name", "Bar do Mock Detalhado",
                "formatted_address", "Rua Falsa, 123"
        );
    }

    // --- Testes de Segurança ---

    @Test
    @DisplayName("Deve falhar ao buscar pins sem token (401 Unauthorized)")
    void deveFalharBuscarPinsSemAutenticacao() throws Exception {
        // --- ARRANGE (Nenhum) ---

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/map/pins/nearby")
                        .param("lat", "-23.427")
                        .param("lng", "-51.938")
                        .param("radius", "1000"))
                .andExpect(status().isUnauthorized()); // Espera um erro 401
    }

    @Test
    @DisplayName("Deve falhar ao buscar detalhes do local sem token (401 Unauthorized)")
    void deveFalharBuscarDetalhesSemAutenticacao() throws Exception {
        // --- ARRANGE (Nenhum) ---

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/map/places/place123"))
                .andExpect(status().isUnauthorized()); // Espera um erro 401
    }

    // --- Testes de Funcionalidade (Autenticados) ---

    @Test
    @DisplayName("Deve buscar pins próximos (GET /map/pins/nearby)")
    void deveBuscarPinsProximos() throws Exception {
        // --- ARRANGE ---
        // Simula o serviço retornando uma lista com o pin mockado
        when(mapService.getPinsNearby(
                eq(-23.427),
                eq(-51.938),
                eq(1000),
                eq("bar") // Testando com a keyword
        )).thenReturn(List.of(mockPin));

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/map/pins/nearby")
                        .param("lat", "-23.427")
                        .param("lng", "-51.938")
                        .param("radius", "1000")
                        .param("keyword", "bar")
                        .with(jwt())) // 5. Simula um usuário logado
                .andExpect(status().isOk()) // Espera 200 OK
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Bar do Mock"))
                .andExpect(jsonPath("$[0].isPartner").value(true));
    }

    @Test
    @DisplayName("Deve buscar detalhes do local (GET /map/places/{placeId})")
    void deveBuscarDetalhesDoLocal() throws Exception {
        // --- ARRANGE ---
        // Simula o serviço retornando o mapa de detalhes
        when(mapService.getPlaceDetailsCached("place123")).thenReturn(mockPlaceDetails);

        // --- ACT & ASSERT ---
        mockMvc.perform(get("/map/places/place123")
                        .with(jwt())) // Simula usuário logado
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bar do Mock Detalhado"))
                .andExpect(jsonPath("$.formatted_address").value("Rua Falsa, 123"));
    }
}