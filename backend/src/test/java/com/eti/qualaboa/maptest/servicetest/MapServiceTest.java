package com.eti.qualaboa.maptest.servicetest;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.map.dto.PinDTO;
import com.eti.qualaboa.map.places.PlacesClient;
import com.eti.qualaboa.map.service.MapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MapServiceTest {

    @Mock
    private EstabelecimentoRepository estRepo;

    @Mock
    private PlacesClient placesClient;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private Cache mockCache;

    @InjectMocks
    private MapService mapService;

    private final double LAT = -23.42;
    private final double LNG = -51.93;
    private final int RADIUS = 1000;
    private final String KEYWORD = "bar";
    private final String CACHE_KEY = "places:" + LAT + ":" + LNG + ":" + RADIUS + ":" + KEYWORD;

    @BeforeEach
    void setUp() {

        when(cacheManager.getCache(anyString())).thenReturn(mockCache);
    }


    private Estabelecimento createMockPartner(Long id, String placeId, String nome) {
        return Estabelecimento.builder()
                .idEstabelecimento(id)
                .placeId(placeId)
                .nome(nome)
                .parceiro(true)
                .latitude(LAT)
                .longitude(LNG)
                .descricao("Descrição do Parceiro")
                .enderecoFormatado("Endereço do Parceiro")
                .build();
    }


    private Map<String, Object> createMockGoogleResponse(String placeId, String nome) {
        Map<String, Object> location = Map.of("lat", LAT, "lng", LNG);
        Map<String, Object> geometry = Map.of("location", location);
        Map<String, Object> place = Map.of(
                "place_id", placeId,
                "name", nome,
                "vicinity", "Endereço do Google",
                "geometry", geometry
        );
        return Map.of("results", List.of(place), "status", "OK");
    }

    @Test
    @DisplayName("Deve buscar parceiros e locais do Google (Cache Miss)")
    void getPinsNearby_CacheMiss_DeveMesclarAmbos() {

        Estabelecimento partner = createMockPartner(1L, "partner_place_id", "Bar do Zé (Parceiro)");
        when(estRepo.findAllWithinRadiusPostGis(LAT, LNG, RADIUS)).thenReturn(List.of(partner));

        Map<String, Object> googleResponse = createMockGoogleResponse("google_place_id", "Boteco Falso (Google)");
        when(placesClient.nearbySearch(LAT, LNG, RADIUS, KEYWORD)).thenReturn(googleResponse);

        when(mockCache.get(CACHE_KEY, Map.class)).thenReturn(null);

        List<PinDTO> pins = mapService.getPinsNearby(LAT, LNG, RADIUS, KEYWORD);

        assertThat(pins).hasSize(2);
        assertThat(pins.get(0).getNome()).isEqualTo("Bar do Zé (Parceiro)");
        assertThat(pins.get(0).getIsPartner()).isTrue();
        assertThat(pins.get(1).getNome()).isEqualTo("Boteco Falso (Google)");
        assertThat(pins.get(1).getIsPartner()).isFalse();

        verify(estRepo).findAllWithinRadiusPostGis(LAT, LNG, RADIUS);
        verify(placesClient).nearbySearch(LAT, LNG, RADIUS, KEYWORD);
        verify(mockCache).put(CACHE_KEY, googleResponse);
    }

    @Test
    @DisplayName("Deve usar dados do cache do Google (Cache Hit)")
    void getPinsNearby_CacheHit_NaoDeveChamarPlacesClient() {

        when(estRepo.findAllWithinRadiusPostGis(LAT, LNG, RADIUS)).thenReturn(Collections.emptyList());

        Map<String, Object> cachedGoogleResponse = createMockGoogleResponse("google_place_id", "Boteco em Cache (Google)");
        when(mockCache.get(CACHE_KEY, Map.class)).thenReturn(cachedGoogleResponse);

        List<PinDTO> pins = mapService.getPinsNearby(LAT, LNG, RADIUS, KEYWORD);


        assertThat(pins).hasSize(1);
        assertThat(pins.get(0).getNome()).isEqualTo("Boteco em Cache (Google)");
        assertThat(pins.get(0).getIsPartner()).isFalse();

        verify(placesClient, never()).nearbySearch(anyDouble(), anyDouble(), anyInt(), anyString());
        verify(mockCache, never()).put(anyString(), any());
    }

    @Test
    @DisplayName("Deve mesclar resultados e remover duplicatas (Parceiro tem prioridade)")
    void getPinsNearby_DeveRemoverDuplicatas() {

        Estabelecimento partner = createMockPartner(1L, "id_duplicado", "Bar do Zé (Parceiro)");
        when(estRepo.findAllWithinRadiusPostGis(LAT, LNG, RADIUS)).thenReturn(List.of(partner));

        Map<String, Object> googleResponse = createMockGoogleResponse("id_duplicado", "Bar do Zé (Google)");
        when(placesClient.nearbySearch(LAT, LNG, RADIUS, KEYWORD)).thenReturn(googleResponse);

        when(mockCache.get(CACHE_KEY, Map.class)).thenReturn(null);

        List<PinDTO> pins = mapService.getPinsNearby(LAT, LNG, RADIUS, KEYWORD);


        assertThat(pins).hasSize(1);
        assertThat(pins.get(0).getNome()).isEqualTo("Bar do Zé (Parceiro)");
        assertThat(pins.get(0).getIsPartner()).isTrue();
    }

    @Test
    @DisplayName("getPlaceDetailsCached deve buscar do cliente (Cache Miss)")
    void getPlaceDetailsCached_CacheMiss() {
        String placeId = "place123";
        Map<String, Object> detailsResponse = Map.of("name", "Detalhes do Bar");

        when(mockCache.get(placeId, Map.class)).thenReturn(null);
        when(placesClient.placeDetails(placeId)).thenReturn(detailsResponse);

        Map<String, Object> result = mapService.getPlaceDetailsCached(placeId);

        assertThat(result).isEqualTo(detailsResponse);
        verify(placesClient).placeDetails(placeId);
        verify(mockCache).put(placeId, detailsResponse);
    }

    @Test
    @DisplayName("getPlaceDetailsCached deve retornar do cache (Cache Hit)")
    void getPlaceDetailsCached_CacheHit() {
        String placeId = "place123";
        Map<String, Object> cachedDetails = Map.of("name", "Detalhes em Cache");

        when(mockCache.get(placeId, Map.class)).thenReturn(cachedDetails);

        Map<String, Object> result = mapService.getPlaceDetailsCached(placeId);

        assertThat(result).isEqualTo(cachedDetails);
        verify(placesClient, never()).placeDetails(placeId);
        verify(mockCache, never()).put(anyString(), any());
    }
}