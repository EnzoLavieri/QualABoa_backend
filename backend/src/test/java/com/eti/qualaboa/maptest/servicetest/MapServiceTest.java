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
    private Cache mockCache; // Mock para o cache individual (ex: "places")

    @InjectMocks
    private MapService mapService;

    // Dados de entrada para os testes
    private final double LAT = -23.42;
    private final double LNG = -51.93;
    private final int RADIUS = 1000;
    private final String KEYWORD = "bar";
    private final String CACHE_KEY = "places:" + LAT + ":" + LNG + ":" + RADIUS + ":" + KEYWORD;

    @BeforeEach
    void setUp() {
        // Configuração global do mock de cache
        // Faz com que cacheManager.getCache("places") ou cacheManager.getCache("placeDetails")
        // sempre retornem o nosso mockCache.
        when(cacheManager.getCache(anyString())).thenReturn(mockCache);
    }

    /**
     * Cria um mock de Estabelecimento parceiro
     */
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

    /**
     * [CORRIGIDO]
     * Cria uma resposta mockada da API do Google Places (o que o PlacesClient retornaria)
     * A estrutura agora inclui "geometry" e "location" para passar na validação do serviço.
     */
    private Map<String, Object> createMockGoogleResponse(String placeId, String nome) {
        Map<String, Object> location = Map.of("lat", LAT, "lng", LNG);
        Map<String, Object> geometry = Map.of("location", location);
        Map<String, Object> place = Map.of(
                "place_id", placeId,
                "name", nome,
                "vicinity", "Endereço do Google",
                "geometry", geometry // <-- ESTA É A CORREÇÃO
        );
        return Map.of("results", List.of(place), "status", "OK");
    }

    @Test
    @DisplayName("Deve buscar parceiros e locais do Google (Cache Miss)")
    void getPinsNearby_CacheMiss_DeveMesclarAmbos() {
        // --- ARRANGE ---
        // 1. Mock do Banco de Dados (Parceiros)
        Estabelecimento partner = createMockPartner(1L, "partner_place_id", "Bar do Zé (Parceiro)");
        when(estRepo.findAllWithinRadiusPostGis(LAT, LNG, RADIUS)).thenReturn(List.of(partner));

        // 2. Mock do Google Places (Não-Parceiros)
        Map<String, Object> googleResponse = createMockGoogleResponse("google_place_id", "Boteco Falso (Google)");
        when(placesClient.nearbySearch(LAT, LNG, RADIUS, KEYWORD)).thenReturn(googleResponse);

        // 3. Mock do Cache (Cache Miss)
        when(mockCache.get(CACHE_KEY, Map.class)).thenReturn(null);

        // --- ACT ---
        List<PinDTO> pins = mapService.getPinsNearby(LAT, LNG, RADIUS, KEYWORD);

        // --- ASSERT ---
        // Deve conter 2 pins: 1 parceiro e 1 do Google (AGORA CORRIGIDO)
        assertThat(pins).hasSize(2);
        // O primeiro deve ser o parceiro (pois ele é adicionado primeiro)
        assertThat(pins.get(0).getNome()).isEqualTo("Bar do Zé (Parceiro)");
        assertThat(pins.get(0).getIsPartner()).isTrue();
        // O segundo deve ser do Google
        assertThat(pins.get(1).getNome()).isEqualTo("Boteco Falso (Google)");
        assertThat(pins.get(1).getIsPartner()).isFalse();

        // Verifica se as funções corretas foram chamadas
        verify(estRepo).findAllWithinRadiusPostGis(LAT, LNG, RADIUS); // Verif. busca no DB
        verify(placesClient).nearbySearch(LAT, LNG, RADIUS, KEYWORD); // Verif. chamada na API
        verify(mockCache).put(CACHE_KEY, googleResponse); // Verif. que salvou no cache
    }

    @Test
    @DisplayName("Deve usar dados do cache do Google (Cache Hit)")
    void getPinsNearby_CacheHit_NaoDeveChamarPlacesClient() {
        // --- ARRANGE ---
        // 1. Mock do Banco de Dados (sem parceiros)
        when(estRepo.findAllWithinRadiusPostGis(LAT, LNG, RADIUS)).thenReturn(Collections.emptyList());

        // 2. Mock do Google Places (em cache)
        Map<String, Object> cachedGoogleResponse = createMockGoogleResponse("google_place_id", "Boteco em Cache (Google)");
        when(mockCache.get(CACHE_KEY, Map.class)).thenReturn(cachedGoogleResponse);

        // --- ACT ---
        List<PinDTO> pins = mapService.getPinsNearby(LAT, LNG, RADIUS, KEYWORD);

        // --- ASSERT ---
        // Deve conter 1 pin (apenas o do cache - AGORA CORRIGIDO)
        assertThat(pins).hasSize(1);
        assertThat(pins.get(0).getNome()).isEqualTo("Boteco em Cache (Google)");
        assertThat(pins.get(0).getIsPartner()).isFalse();

        // Verifica se as chamadas de API foram EVITADAS
        verify(placesClient, never()).nearbySearch(anyDouble(), anyDouble(), anyInt(), anyString()); // NUNCA chamou a API
        verify(mockCache, never()).put(anyString(), any()); // NUNCA salvou no cache (pois já estava lá)
    }

    @Test
    @DisplayName("Deve mesclar resultados e remover duplicatas (Parceiro tem prioridade)")
    void getPinsNearby_DeveRemoverDuplicatas() {
        // --- ARRANGE ---
        // 1. Mock do Banco de Dados (Parceiro)
        // O parceiro tem o placeId "id_duplicado"
        Estabelecimento partner = createMockPartner(1L, "id_duplicado", "Bar do Zé (Parceiro)");
        when(estRepo.findAllWithinRadiusPostGis(LAT, LNG, RADIUS)).thenReturn(List.of(partner));

        // 2. Mock do Google (também retorna o "id_duplicado")
        Map<String, Object> googleResponse = createMockGoogleResponse("id_duplicado", "Bar do Zé (Google)");
        when(placesClient.nearbySearch(LAT, LNG, RADIUS, KEYWORD)).thenReturn(googleResponse);

        // 3. Mock do Cache (Cache Miss)
        when(mockCache.get(CACHE_KEY, Map.class)).thenReturn(null);

        // --- ACT ---
        List<PinDTO> pins = mapService.getPinsNearby(LAT, LNG, RADIUS, KEYWORD);

        // --- ASSERT ---
        // Deve conter APENAS 1 pin, pois o placeId é o mesmo
        assertThat(pins).hasSize(1);
        // O pin deve ser o do PARCEIRO (que tem prioridade)
        assertThat(pins.get(0).getNome()).isEqualTo("Bar do Zé (Parceiro)");
        assertThat(pins.get(0).getIsPartner()).isTrue();
    }

    @Test
    @DisplayName("getPlaceDetailsCached deve buscar do cliente (Cache Miss)")
    void getPlaceDetailsCached_CacheMiss() {
        // --- ARRANGE ---
        String placeId = "place123";
        Map<String, Object> detailsResponse = Map.of("name", "Detalhes do Bar");

        when(mockCache.get(placeId, Map.class)).thenReturn(null); // Cache miss
        when(placesClient.placeDetails(placeId)).thenReturn(detailsResponse); // Mock do client

        // --- ACT ---
        Map<String, Object> result = mapService.getPlaceDetailsCached(placeId);

        // --- ASSERT ---
        assertThat(result).isEqualTo(detailsResponse);
        verify(placesClient).placeDetails(placeId); // Verif. que chamou a API
        verify(mockCache).put(placeId, detailsResponse); // Verif. que salvou no cache
    }

    @Test
    @DisplayName("getPlaceDetailsCached deve retornar do cache (Cache Hit)")
    void getPlaceDetailsCached_CacheHit() {
        // --- ARRANGE ---
        String placeId = "place123";
        Map<String, Object> cachedDetails = Map.of("name", "Detalhes em Cache");

        when(mockCache.get(placeId, Map.class)).thenReturn(cachedDetails); // Cache hit

        // --- ACT ---
        Map<String, Object> result = mapService.getPlaceDetailsCached(placeId);

        // --- ASSERT ---
        assertThat(result).isEqualTo(cachedDetails);
        verify(placesClient, never()).placeDetails(placeId); // Verif. que NÃO chamou a API
        verify(mockCache, never()).put(anyString(), any()); // Verif. que NÃO salvou no cache
    }
}