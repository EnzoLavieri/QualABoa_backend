package com.eti.qualaboa.map.service;

import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.estabelecimento.repository.EstabelecimentoRepository;
import com.eti.qualaboa.map.dto.PinDTO;
import com.eti.qualaboa.map.places.PlacesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MapService {

    private final EstabelecimentoRepository estRepo;
    private final PlacesClient placesClient;
    private final CacheManager cacheManager;

    public List<PinDTO> getPinsNearby(double lat, double lng, int radiusMeters, String keyword) {
        log.info("Buscando locais próximos de ({}, {}) com raio {}m e keyword='{}'", lat, lng, radiusMeters, keyword);

        // 🔹 1. Buscar parceiros no banco
        List<Estabelecimento> partners = estRepo.findAllWithinRadiusPostGis(lat, lng, radiusMeters)
                .stream()
                .filter(e -> Boolean.TRUE.equals(e.getParceiro()))
                .collect(Collectors.toList());

        Map<String, PinDTO> map = new LinkedHashMap<>();

        for (Estabelecimento e : partners) {
            PinDTO p = PinDTO.builder()
                    .id(e.getIdEstabelecimento())
                    .placeId(e.getPlaceId())
                    .nome(e.getNome())
                    .lat(e.getLatitude())
                    .lng(e.getLongitude())
                    .isPartner(true)
                    .snippet(e.getDescricao())
                    .endereco(e.getEnderecoFormatado())
                    .build();
            map.put(e.getPlaceId() != null ? e.getPlaceId() : "local-" + e.getIdEstabelecimento(), p);
        }

        // 🔹 2. Buscar locais no Google Places API
        String cacheKey = "places:" + lat + ":" + lng + ":" + radiusMeters + ":" + (keyword == null ? "" : keyword);
        Map<String, Object> placesResp = getPlacesCached(cacheKey, lat, lng, radiusMeters, keyword);

        if (placesResp != null && placesResp.containsKey("results")) {
            Object rawResults = placesResp.get("results");
            if (rawResults instanceof List<?>) {
                List<?> results = (List<?>) rawResults;
                log.info("Foram encontrados {} resultados do Google Places.", results.size());

                for (Object obj : results) {
                    if (!(obj instanceof Map<?, ?> r)) continue;

                    String placeId = (String) r.get("place_id");
                    if (map.containsKey(placeId)) continue; // evita duplicar parceiros

                    Map<String, Object> geometry = (Map<String, Object>) r.get("geometry");
                    if (geometry == null) continue;

                    Map<String, Object> location = (Map<String, Object>) geometry.get("location");
                    if (location == null) continue;

                    Double plat = ((Number) location.get("lat")).doubleValue();
                    Double plng = ((Number) location.get("lng")).doubleValue();

                    String name = (String) r.get("name");
                    String vicinity = (String) r.get("vicinity");

                    map.put(placeId, PinDTO.builder()
                            .id(null)
                            .placeId(placeId)
                            .nome(name)
                            .lat(plat)
                            .lng(plng)
                            .isPartner(false)
                            .snippet(vicinity)
                            .endereco(vicinity)
                            .build());
                }
            } else {
                log.warn("Campo 'results' inesperado na resposta do Google: {}", rawResults);
            }
        } else {
            log.warn("Nenhum resultado retornado do Google Places (status={})",
                    placesResp != null ? placesResp.get("status") : "null");
        }

        return new ArrayList<>(map.values());
    }

    private Map<String, Object> getPlacesCached(String cacheKey, double lat, double lng, int radius, String keyword) {
        Cache cache = cacheManager.getCache("places");
        if (cache != null) {
            Map<String, Object> cached = cache.get(cacheKey, Map.class);
            if (cached != null) {
                log.info("Cache HIT para {}", cacheKey);
                return cached;
            }
        }

        Map<String, Object> resp = placesClient.nearbySearch(lat, lng, radius,
                keyword == null ? "bar|restaurant" : keyword);
        if (cache != null && resp != null) {
            cache.put(cacheKey, resp);
        }
        return resp;
    }

    public Map<String, Object> getPlaceDetailsCached(String placeId) {
        Cache cache = cacheManager.getCache("placeDetails");
        if (cache != null) {
            Map<String, Object> cached = cache.get(placeId, Map.class);
            if (cached != null) {
                log.info("Cache HIT para detalhes {}", placeId);
                return cached;
            }
        }

        Map<String, Object> details = placesClient.placeDetails(placeId);
        if (cache != null && details != null) {
            cache.put(placeId, details);
        }
        return details;
    }

    public Map<String, Object> getPlaceReviewsCached(String placeId) {
        Cache cache = cacheManager.getCache("placeReviews");
        if (cache != null) {
            Map<String, Object> cached = cache.get(placeId, Map.class);
            if (cached != null) {
                log.info("Cache HIT para reviews {}", placeId);
                return cached;
            }
        }

        Map<String, Object> reviews = placesClient.getPlaceReviews(placeId);

        if (cache != null && reviews != null) {
            cache.put(placeId, reviews);
        }

        return reviews;
    }
}
