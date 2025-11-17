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


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {CacheConfig.class})
public class CacheConfigTest {

    @Autowired
    private CacheManager cacheManager;

    @Test
    @DisplayName("Deve carregar o CacheManager como um bean")
    void deveCarregarCacheManager() {
        assertThat(cacheManager).isNotNull();

        assertThat(cacheManager).isInstanceOf(CaffeineCacheManager.class);
    }

    @Test
    @DisplayName("Deve ter os caches 'places' e 'placeDetails' pré-configurados")
    void deveTerCachesPreConfigurados() {

        assertThat(cacheManager.getCacheNames()).contains("places", "placeDetails");
    }

    @Test
    @DisplayName("Deve ser capaz de usar os caches (put, get, evict)")
    void deveArmazenarEBuscarDadosDoCache() {

        Cache placesCache = cacheManager.getCache("places");
        assertThat(placesCache).isNotNull();

        String key = "test-key-123";
        String value = "test-value";


        placesCache.put(key, value);


        assertThat(placesCache.get(key)).isNotNull();
        assertThat(placesCache.get(key).get()).isEqualTo(value);


        placesCache.evict(key);


        assertThat(placesCache.get(key)).isNull();
    }
}