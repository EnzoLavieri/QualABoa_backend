package com.eti.qualaboa.enderecotest;

import com.eti.qualaboa.endereco.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = EnderecoTest.Initializer.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@EntityScan(basePackages = "com.eti.qualaboa")
public class EnderecoTest {

    @Autowired
    private TestEntityManager testEntityManager;


    @Container
    private static final PostgreSQLContainer<?> postgresContainer;

    static {
        DockerImageName postgisImage = DockerImageName.parse("postgis/postgis:15-3.3")
                .asCompatibleSubstituteFor("postgres");

        postgresContainer = new PostgreSQLContainer<>(postgisImage)
                .withDatabaseName("qualaboa")
                .withUsername("qualaboa_user")
                .withPassword("senha123");
        postgresContainer.start();
    }

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresContainer.getUsername(),
                    "spring.datasource.password=" + postgresContainer.getPassword(),
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
                    "spring.jpa.hibernate.ddl-auto=create-drop"
            );
        }
    }


    @Test
    @DisplayName("Deve persistir e recuperar um Endereco")
    void devePersistirERecuperarEndereco() {
        Endereco endereco = Endereco.builder()
                .rua("Avenida Brasil")
                .numero("1234")
                .bairro("Zona 01")
                .cidade("Maringá")
                .estado("PR")
                .cep("87013-000")
                .latitude(-23.4273)
                .longitude(-51.9389)
                .build();


        Endereco enderecoSalvo = testEntityManager.persistAndFlush(endereco);
        Long idSalvo = enderecoSalvo.getIdEndereco();

        testEntityManager.clear();


        Endereco enderecoDoBanco = testEntityManager.find(Endereco.class, idSalvo);

        assertThat(enderecoDoBanco).isNotNull();
        assertThat(enderecoDoBanco.getIdEndereco()).isEqualTo(idSalvo);
        assertThat(enderecoDoBanco.getRua()).isEqualTo("Avenida Brasil");
        assertThat(enderecoDoBanco.getCidade()).isEqualTo("Maringá");
        assertThat(enderecoDoBanco.getLatitude()).isEqualTo(-23.4273);
    }
}