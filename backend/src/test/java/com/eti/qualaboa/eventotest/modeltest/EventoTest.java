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
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = EventoTest.Initializer.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@EntityScan(basePackages = "com.eti.qualaboa")
public class EventoTest {

    @Autowired
    private TestEntityManager testEntityManager;

    private Estabelecimento estSalvo;
    private Cupom cupomSalvo;


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



    @BeforeEach
    void setUpDependencies() {
        Estabelecimento est = new Estabelecimento();
        est.setNome("Bar do Teste de Evento");
        est.setEmail("evento@teste.com");
        est.setSenha("123");
        this.estSalvo = testEntityManager.persist(est);

        Cupom cupom = new Cupom();
        cupom.setCodigo("EVENTO10");
        cupom.setTipo(TipoCupom.DESCONTO);
        cupom.setEstabelecimento(this.estSalvo);
        this.cupomSalvo = testEntityManager.persist(cupom);

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Test
    @DisplayName("Deve persistir Evento com Cupom e Estabelecimento")
    void devePersistirEventoCompleto() {


        Evento evento = Evento.builder()
                .titulo("Show de Rock")
                .descricao("Evento de teste com cupom")
                .data("2025-12-25")
                .horario("21:00")
                .estabelecimento(estSalvo)
                .cupom(cupomSalvo)
                .build();

        Evento eventoSalvo = testEntityManager.persistAndFlush(evento);
        Long eventoId = eventoSalvo.getIdEvento();
        testEntityManager.clear();

        Evento eventoDoBanco = testEntityManager.find(Evento.class, eventoId);

        assertThat(eventoDoBanco).isNotNull();
        assertThat(eventoDoBanco.getTitulo()).isEqualTo("Show de Rock");
        assertThat(eventoDoBanco.getData()).isEqualTo("2025-12-25");
        assertThat(eventoDoBanco.getTotalCliques()).isEqualTo(0);

        assertThat(eventoDoBanco.getEstabelecimento()).isNotNull();
        assertThat(eventoDoBanco.getEstabelecimento().getIdEstabelecimento()).isEqualTo(estSalvo.getIdEstabelecimento());
        assertThat(eventoDoBanco.getCupom()).isNotNull();
        assertThat(eventoDoBanco.getCupom().getIdCupom()).isEqualTo(cupomSalvo.getIdCupom());
    }

    @Test
    @DisplayName("Deve persistir Evento sem Cupom")
    void devePersistirEventoSemCupom() {


        Evento evento = Evento.builder()
                .titulo("Pagode Acústico")
                .descricao("Evento sem cupom associado")
                .data("2025-11-20")
                .horario("19:00")
                .estabelecimento(estSalvo)
                .cupom(null)
                .build();

        Evento eventoSalvo = testEntityManager.persistAndFlush(evento);
        Long eventoId = eventoSalvo.getIdEvento();
        testEntityManager.clear();

        Evento eventoDoBanco = testEntityManager.find(Evento.class, eventoId);

        assertThat(eventoDoBanco).isNotNull();
        assertThat(eventoDoBanco.getTitulo()).isEqualTo("Pagode Acústico");
        assertThat(eventoDoBanco.getEstabelecimento()).isNotNull();
        assertThat(eventoDoBanco.getEstabelecimento().getIdEstabelecimento()).isEqualTo(estSalvo.getIdEstabelecimento());

        assertThat(eventoDoBanco.getCupom()).isNull();
    }
}