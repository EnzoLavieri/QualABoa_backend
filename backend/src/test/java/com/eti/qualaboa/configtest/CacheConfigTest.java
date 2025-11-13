package com.eti.qualaboa.configtest;

import com.eti.qualaboa.config.CacheConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Teste unitário para a classe CacheConfig.
 * Usamos @ContextConfiguration para carregar *apenas* a classe CacheConfig,
 * isolando-a do resto da aplicação (como o banco de dados).
 */
@ExtendWith(SpringExtension.class) // Habilita o Spring para o teste
@ContextConfiguration(classes = {CacheConfig.class}) // Carrega SÓ a nossa config de cache
public class CacheConfigTest {

    // Injeta o bean que foi criado pela CacheConfig
    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Deve carregar o CacheManager como um bean")
    void deveCarregarCacheManager() {
        // Verifica se o bean foi injetado
        assertThat(cacheManager).isNotNull();

        // Verifica se é a implementação correta (Caffeine)
        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    @DisplayName("Deve ter os caches 'places' e 'placeDetails' pré-configurados")
    void deveTerCachesPreConfigurados() {
        // A config original define "places" e "placeDetails"
        // O CaffeineCacheManager também cria caches sob demanda,
        // mas os nomes pré-definidos devem estar presentes.
        assertThat(cacheManager.getCacheNames()).contains("places", "placeDetails");
    }

    @Test
    @DisplayName("Deve ser capaz de usar os caches (put, get, evict)")
    void deveArmazenarEBuscarDadosDoCache() {
        // --- ARRANGE ---
        // Pega um dos caches pré-definidos
        Cache placesCache = cacheManager.getCache("places");
        assertThat(placesCache).isNotNull();

        String key = "test-key-123";
        String value = "test-value";

        // --- ACT (PUT) ---
        placesCache.put(key, value);

        // --- ASSERT (GET) ---
        // Verifica se o valor foi armazenado
        assertThat(placesCache.get(key)).isNotNull();
        assertThat(placesCache.get(key).get()).isEqualTo(value);

        // --- ACT 2 (EVICT) ---
        placesCache.evict(key);

        // --- ASSERT 2 (GET after EVICT) ---
        // Verifica se foi removido
        assertThat(placesCache.get(key)).isNull();
    }
}