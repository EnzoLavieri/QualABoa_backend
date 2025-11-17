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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MapController.class)
@Import(SecurityConfig.class)
public class MapControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MapService mapService;

    private PinDTO mockPin;
    private Map<String, Object> mockPlaceDetails;

    @BeforeEach
    void setUp() {
        mockPin = PinDTO.builder()
                .id(1L)
                .placeId("place123")
                .nome("Bar do Mock")
                .lat(-23.427)
                .lng(-51.938)
                .isPartner(true)
                .build();


        mockPlaceDetails = Map.of(
                "name", "Bar do Mock Detalhado",
                "formatted_address", "Rua Falsa, 123"
        );
    }


    @Test
    @DisplayName("Deve falhar ao buscar pins sem token (401 Unauthorized)")
    void deveFalharBuscarPinsSemAutenticacao() throws Exception {



        mockMvc.perform(get("/map/pins/nearby")
                        .param("lat", "-23.427")
                        .param("lng", "-51.938")
                        .param("radius", "1000"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Deve falhar ao buscar detalhes do local sem token (401 Unauthorized)")
    void deveFalharBuscarDetalhesSemAutenticacao() throws Exception {

        mockMvc.perform(get("/map/places/place123"))
                .andExpect(status().isUnauthorized());
    }



    @Test
    @DisplayName("Deve buscar pins próximos (GET /map/pins/nearby)")
    void deveBuscarPinsProximos() throws Exception {

        when(mapService.getPinsNearby(
                eq(-23.427),
                eq(-51.938),
                eq(1000),
                eq("bar")
        )).thenReturn(List.of(mockPin));


        mockMvc.perform(get("/map/pins/nearby")
                        .param("lat", "-23.427")
                        .param("lng", "-51.938")
                        .param("radius", "1000")
                        .param("keyword", "bar")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].nome").value("Bar do Mock"))
                .andExpect(jsonPath("$[0].isPartner").value(true));
    }

    @Test
    @DisplayName("Deve buscar detalhes do local (GET /map/places/{placeId})")
    void deveBuscarDetalhesDoLocal() throws Exception {

        when(mapService.getPlaceDetailsCached("place123")).thenReturn(mockPlaceDetails);


        mockMvc.perform(get("/map/places/place123")
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Bar do Mock Detalhado"))
                .andExpect(jsonPath("$.formatted_address").value("Rua Falsa, 123"));
    }
}