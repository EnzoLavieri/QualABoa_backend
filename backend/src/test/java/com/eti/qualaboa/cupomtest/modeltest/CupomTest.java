package com.eti.qualaboa.cupomtest.modeltest;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.enums.TipoCupom;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import org.junit.jupiter.api.BeforeEach;
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

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = CupomTest.Initializer.class)
@TestPropertySource(properties = "spring.sql.init.mode=never") // Desabilita o data.sql
@EntityScan(basePackages = "com.eti.qualaboa") // Garante que todas as entidades sejam escaneadas
public class CupomTest {

    @Autowired
    private TestEntityManager testEntityManager;

    // Dependência obrigatória para um Cupom
    private Estabelecimento estSalvo;

    // --- Configuração do Testcontainers (Padrão) ---

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

    // --- Fim da Configuração ---

    /**
     * Salva a dependência (Estabelecimento) antes de cada teste.
     */
    @BeforeEach
    void setUpDependencies() {
        Estabelecimento est = new Estabelecimento();
        est.setNome("Bar do Teste de Cupom");
        est.setEmail("cupom@teste.com");
        est.setSenha("123");
        this.estSalvo = testEntityManager.persistFlushFind(est); // Salva e recupera com ID
        testEntityManager.clear(); // Limpa o cache
    }

    @Test
    @DisplayName("Deve persistir um Cupom com Estabelecimento")
    void devePersistirCupomCompleto() {
        // --- ARRANGE ---
        // O estSalvo já foi criado no @BeforeEach
        LocalDateTime dataInicio = LocalDateTime.of(2025, 12, 1, 0, 0);
        LocalDateTime dataFim = LocalDateTime.of(2025, 12, 25, 23, 59);

        Cupom cupom = Cupom.builder()
                .codigo("NATAL25")
                .descricao("Desconto de Natal")
                .tipo(TipoCupom.DESCONTO)
                .valor(25.0)
                .dataInicio(dataInicio)
                .dataFim(dataFim)
                .ativo(true)
                .quantidadeTotal(100)
                .quantidadeUsada(0)
                .estabelecimento(estSalvo) // Relacionamento @ManyToOne
                .build();

        // --- ACT ---
        Cupom cupomSalvo = testEntityManager.persistAndFlush(cupom);
        Long idSalvo = cupomSalvo.getIdCupom();
        testEntityManager.clear(); // Limpa o cache

        // --- ASSERT ---
        Cupom cupomDoBanco = testEntityManager.find(Cupom.class, idSalvo);

        assertThat(cupomDoBanco).isNotNull();
        assertThat(cupomDoBanco.getIdCupom()).isEqualTo(idSalvo);
        assertThat(cupomDoBanco.getCodigo()).isEqualTo("NATAL25");
        assertThat(cupomDoBanco.getTipo()).isEqualTo(TipoCupom.DESCONTO);
        assertThat(cupomDoBanco.getValor()).isEqualTo(25.0);
        assertThat(cupomDoBanco.getDataFim()).isEqualTo(dataFim);
        assertThat(cupomDoBanco.isAtivo()).isTrue();
        assertThat(cupomDoBanco.getQuantidadeUsada()).isEqualTo(0); // Verifica valor default

        // Verifica o relacionamento
        assertThat(cupomDoBanco.getEstabelecimento()).isNotNull();
        assertThat(cupomDoBanco.getEstabelecimento().getIdEstabelecimento()).isEqualTo(estSalvo.getIdEstabelecimento());
        assertThat(cupomDoBanco.getEstabelecimento().getNome()).isEqualTo("Bar do Teste de Cupom");
    }
}