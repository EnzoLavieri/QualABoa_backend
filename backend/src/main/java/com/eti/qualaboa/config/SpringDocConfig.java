package com.eti.qualaboa.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuração do SpringDoc (Swagger) para definir informações da API
 * e o esquema de segurança JWT (Bearer Token).
 */
@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {

        // Define o nome do esquema de segurança
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                // Adiciona informações gerais da API (título, versão)
                .info(new Info()
                        .title("QualABoa API")
                        .version("v2.0")
                        .description("Documentação da API do projeto QualABoa")
                )
                // Adiciona a definição do esquema de segurança (JWT)
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP) // Tipo HTTP
                                        .scheme("bearer")               // Esquema Bearer
                                        .bearerFormat("JWT")            // Formato JWT
                        )
                )
                // Adiciona o requisito de segurança global a todos os endpoints
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}