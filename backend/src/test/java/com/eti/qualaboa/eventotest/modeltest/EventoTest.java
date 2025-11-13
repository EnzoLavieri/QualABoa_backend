package com.eti.qualaboa.eventotest.modeltest;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.enums.TipoCupom;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.evento.model.Evento;
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

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest // Foca apenas na camada de persistência JPA
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Desliga o H2
@ContextConfiguration(initializers = EventoTest.Initializer.class) // Aplica as configs do container
@TestPropertySource(properties = "spring.sql.init.mode=never") // Desabilita o data.sql
@EntityScan(basePackages = "com.eti.qualaboa") // Escaneia todas as entidades
public class EventoTest {

    @Autowired
    private TestEntityManager testEntityManager;

    private Estabelecimento estSalvo;
    private Cupom cupomSalvo;

    // --- Configuração do Testcontainers (igual aos outros testes) ---

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

    // --- Fim da Configuração do Testcontainers ---


    /**
     * Prepara as dependências (Estabelecimento e Cupom) antes de cada teste.
     */
    @BeforeEach
    void setUpDependencies() {
        // 1. Criar e salvar a dependência obrigatória: Estabelecimento
        Estabelecimento est = new Estabelecimento();
        est.setNome("Bar do Teste de Evento");
        est.setEmail("evento@teste.com");
        est.setSenha("123");
        this.estSalvo = testEntityManager.persist(est);

        // 2. Criar e salvar a dependência opcional: Cupom
        Cupom cupom = new Cupom();
        cupom.setCodigo("EVENTO10");
        cupom.setTipo(TipoCupom.DESCONTO);
        cupom.setEstabelecimento(this.estSalvo); // Cupom também precisa de um estabelecimento
        this.cupomSalvo = testEntityManager.persist(cupom);

        // Garante que o flush e clear ocorram antes do próximo teste
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("Deve persistir Evento com Cupom e Estabelecimento")
    void devePersistirEventoCompleto() {
        // --- ARRANGE ---
        // As dependências (estSalvo e cupomSalvo) já foram criadas no @BeforeEach

        Evento evento = Evento.builder()
                .titulo("Show de Rock")
                .descricao("Evento de teste com cupom")
                .data("2025-12-25")
                .horario("21:00")
                .estabelecimento(estSalvo)
                .cupom(cupomSalvo)
                .build();

        // --- ACT ---
        Evento eventoSalvo = testEntityManager.persistAndFlush(evento);
        Long eventoId = eventoSalvo.getIdEvento();
        testEntityManager.clear(); // Limpa o cache para forçar a busca no DB

        // --- ASSERT ---
        Evento eventoDoBanco = testEntityManager.find(Evento.class, eventoId);

        assertThat(eventoDoBanco).isNotNull();
        assertThat(eventoDoBanco.getTitulo()).isEqualTo("Show de Rock");
        assertThat(eventoDoBanco.getData()).isEqualTo("2025-12-25");
        assertThat(eventoDoBanco.getTotalCliques()).isEqualTo(0); // Verifica valor default

        // Verifica os relacionamentos
        assertThat(eventoDoBanco.getEstabelecimento()).isNotNull();
        assertThat(eventoDoBanco.getEstabelecimento().getIdEstabelecimento()).isEqualTo(estSalvo.getIdEstabelecimento());
        assertThat(eventoDoBanco.getCupom()).isNotNull();
        assertThat(eventoDoBanco.getCupom().getIdCupom()).isEqualTo(cupomSalvo.getIdCupom());
    }

    @Test
    @DisplayName("Deve persistir Evento sem Cupom")
    void devePersistirEventoSemCupom() {
        // --- ARRANGE ---
        // O estSalvo já foi criado no @BeforeEach

        Evento evento = Evento.builder()
                .titulo("Pagode Acústico")
                .descricao("Evento sem cupom associado")
                .data("2025-11-20")
                .horario("19:00")
                .estabelecimento(estSalvo)
                .cupom(null) // Testa o caso onde o cupom é nulo
                .build();

        // --- ACT ---
        Evento eventoSalvo = testEntityManager.persistAndFlush(evento);
        Long eventoId = eventoSalvo.getIdEvento();
        testEntityManager.clear();

        // --- ASSERT ---
        Evento eventoDoBanco = testEntityManager.find(Evento.class, eventoId);

        assertThat(eventoDoBanco).isNotNull();
        assertThat(eventoDoBanco.getTitulo()).isEqualTo("Pagode Acústico");
        assertThat(eventoDoBanco.getEstabelecimento()).isNotNull();
        assertThat(eventoDoBanco.getEstabelecimento().getIdEstabelecimento()).isEqualTo(estSalvo.getIdEstabelecimento());

        // Verifica a asserção principal
        assertThat(eventoDoBanco.getCupom()).isNull();
    }
}