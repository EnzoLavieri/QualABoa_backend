package com.eti.qualaboa.map.places;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class PlacesClient {

    private  WebClient webClient = WebClient.builder().build();

    @Value("${google.places.apiKey}")
    private String apiKey;

    public Map<String, Object> nearbySearch(double lat, double lng, int radius, String keyword) {
        String url = UriComponentsBuilder.fromUriString("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                .queryParam("key", apiKey)
                .queryParam("location", lat + "," + lng)
                .queryParam("radius", radius)
                .queryParam("keyword", keyword)
                .build().toUriString();

        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Map<String, Object> placeDetails(String placeId) {
        String url = UriComponentsBuilder.fromUriString("https://maps.googleapis.com/maps/api/place/details/json")
                .queryParam("key", apiKey)
                .queryParam("place_id", placeId)
                .queryParam("fields", "place_id,name,formatted_address,geometry,formatted_phone_number,website,opening_hours")
                .build().toUriString();

        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Map<String, Object> getPlaceReviews(String placeId) {
        String baseUrl = "https://maps.googleapis.com/maps/api/place/details/json";
        String fields = "name,rating,reviews,user_ratings_total";

        String url = UriComponentsBuilder.fromUriString(baseUrl)
                .queryParam("key", apiKey)
                .queryParam("place_id", placeId)
                .queryParam("fields", fields)
                .build().toUriString();

        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }

    public Map<String, Object> textSearch(String query, double lat, double lng, int radius) {
        String url = UriComponentsBuilder.fromUriString("https://maps.googleapis.com/maps/api/place/textsearch/json")
                .queryParam("key", apiKey)
                .queryParam("query", query)
                .queryParam("location", lat + "," + lng)
                .queryParam("radius", radius)
                .build().toUriString();

        return webClient.get().uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .block();
    }
}
