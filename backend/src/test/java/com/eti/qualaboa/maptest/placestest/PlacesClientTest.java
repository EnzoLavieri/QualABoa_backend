package com.eti.qualaboa.maptest.placestest;

import com.eti.qualaboa.map.places.PlacesClient;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Teste unitário para o PlacesClient.
 * Este teste usa o MockWebServer para simular as respostas da API do Google,
 * evitando chamadas de rede reais.
 */
public class PlacesClientTest {

    // Servidor web falso que interceptará as chamadas
    public static MockWebServer mockWebServer;

    // A classe que estamos testando
    private PlacesClient placesClient;

    @BeforeAll
    static void setUpServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(); // Inicia o servidor em uma porta aleatória
    }

    @AfterAll
    static void tearDownServer() throws IOException {
        mockWebServer.shutdown(); // Desliga o servidor
    }

    @BeforeEach
    void setUpClient() {
        // Antes de cada teste, criamos uma nova instância do PlacesClient
        placesClient = new PlacesClient();
        String baseUrl = String.format("http://localhost:%s", mockWebServer.getPort());

        // --- Injeção de Dependência via Reflexão ---
        // Como o PlacesClient cria seu próprio WebClient e usa @Value,
        // precisamos usar ReflectionTestUtils para injetar nossos mocks.

        // 1. Injeta uma API Key falsa
        ReflectionTestUtils.setField(placesClient, "apiKey", "TEST_API_KEY");

        // 2. Cria um novo WebClient apontando para o nosso servidor falso
        WebClient mockWebClient = WebClient.builder()
                .baseUrl(baseUrl) // Aponta para http://localhost:PORTA_ALEATORIA
                .build();

        // 3. Substitui o WebClient privado dentro do placesClient
        ReflectionTestUtils.setField(placesClient, "webClient", mockWebClient);
    }

    @Test
    @DisplayName("nearbySearch deve construir URL correta e analisar a resposta")
    void nearbySearch_DeveConstruirUrlCorretaEAnalisarResposta() throws Exception {
        // --- ARRANGE (Preparação) ---

        // 1. Define o JSON que o servidor falso deve responder
        String mockJsonResponse = """
                {
                    "results": [
                        { "name": "Bar Falso", "place_id": "fake_place_id_123" }
                    ],
                    "status": "OK"
                }
                """;

        // 2. Coloca a resposta na fila do servidor
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockJsonResponse)
                .addHeader("Content-Type", "application/json"));

        // --- ACT (Ação) ---
        // 3. Chama o método que queremos testar
        Map<String, Object> response = placesClient.nearbySearch(-23.1, -51.2, 1500, "bar");

        // --- ASSERT (Verificação) ---

        // 4. Verifica se o método analisou (parse) o JSON corretamente
        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("OK");
        assertThat(response.get("results")).isInstanceOf(List.class);
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        assertThat(results.get(0).get("name")).isEqualTo("Bar Falso");

        // 5. Verifica se o PlacesClient montou a URL correta
        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertAll(
                () -> assertThat(path).startsWith("/maps/api/place/nearbysearch/json"),
                () -> assertThat(path).contains("key=TEST_API_KEY"),
                () -> assertThat(path).contains("location=-23.1,-51.2"),
                () -> assertThat(path).contains("radius=1500"),
                () -> assertThat(path).contains("keyword=bar")
        );
    }

    @Test
    @DisplayName("placeDetails deve construir URL correta e analisar a resposta")
    void placeDetails_DeveConstruirUrlCorretaEAnalisarResposta() throws Exception {
        // --- ARRANGE ---
        String mockJsonResponse = """
                {
                    "result": {
                        "name": "Bar Falso Detalhado",
                        "formatted_address": "Rua Falsa, 123"
                    },
                    "status": "OK"
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockJsonResponse)
                .addHeader("Content-Type", "application/json"));

        // --- ACT ---
        Map<String, Object> response = placesClient.placeDetails("fake_id_456");

        // --- ASSERT ---
        // Verifica a resposta
        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("OK");
        Map<String, Object> result = (Map<String, Object>) response.get("result");
        assertThat(result.get("name")).isEqualTo("Bar Falso Detalhado");

        // Verifica a URL chamada
        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertAll(
                () -> assertThat(path).startsWith("/maps/api/place/details/json"),
                () -> assertThat(path).contains("key=TEST_API_KEY"),
                () -> assertThat(path).contains("place_id=fake_id_456"),
                () -> assertThat(path).contains("fields=place_id,name,formatted_address,geometry,formatted_phone_number,website,opening_hours")
        );
    }

    @Test
    @DisplayName("textSearch deve construir URL correta e analisar a resposta")
    void textSearch_DeveConstruirUrlCorretaEAnalisarResposta() throws Exception {
        // --- ARRANGE ---
        String mockJsonResponse = """
                {
                    "results": [ { "name": "Resultado da Busca por Texto" } ],
                    "status": "OK"
                }
                """;
        mockWebServer.enqueue(new MockResponse()
                .setBody(mockJsonResponse)
                .addHeader("Content-Type", "application/json"));

        // --- ACT ---
        Map<String, Object> response = placesClient.textSearch("pizza perto de mim", -23.5, -51.5, 5000);

        // --- ASSERT ---
        // Verifica a resposta
        assertThat(response).isNotNull();
        assertThat(response.get("status")).isEqualTo("OK");
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        assertThat(results.get(0).get("name")).isEqualTo("Resultado da Busca por Texto");

        // Verifica a URL chamada
        RecordedRequest request = mockWebServer.takeRequest();
        String path = request.getPath();
        assertAll(
                () -> assertThat(path).startsWith("/maps/api/place/textsearch/json"),
                () -> assertThat(path).contains("key=TEST_API_KEY"),
                () -> assertThat(path).contains("query=pizza+perto+de+mim"), // Verifica a codificação do espaço
                () -> assertThat(path).contains("location=-23.5,-51.5"),
                () -> assertThat(path).contains("radius=5000")
        );
    }
}