package com.eti.qualaboa.promocaotest.modeltest;

import com.eti.qualaboa.cupom.model.Cupom;
import com.eti.qualaboa.enums.TipoCupom;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.promocao.model.Promocao;
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
@ContextConfiguration(initializers = PromocaoTest.Initializer.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@EntityScan(basePackages = "com.eti.qualaboa")
public class PromocaoTest {

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
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect"
            );
        }
    }


    @Test
    @DisplayName("Deve persistir promocao com cupom e estabelecimento")
    void devePersistirPromocaoCompleta() {

        Estabelecimento est = new Estabelecimento();
        est.setNome("Bar do Teste");
        est.setEmail("bar@teste.com");
        est.setSenha("123");
        Estabelecimento estSalvo = testEntityManager.persist(est);

        Cupom cupom = new Cupom();
        cupom.setCodigo("PROMO10");
        cupom.setTipo(TipoCupom.DESCONTO);
        cupom.setEstabelecimento(estSalvo);
        Cupom cupomSalvo = testEntityManager.persist(cupom);

        Promocao promocao = Promocao.builder()
                .estabelecimento(estSalvo)
                .cupom(cupomSalvo)
                .descricao("Happy Hour Teste")
                .desconto(15.0)
                .ativa(true)
                .build();


        Promocao promoSalva = testEntityManager.persistAndFlush(promocao);
        Long idPromocao = promoSalva.getIdPromocao();

        testEntityManager.clear();


        Promocao promoDoBanco = testEntityManager.find(Promocao.class, idPromocao);

        assertThat(promoDoBanco).isNotNull();
        assertThat(promoDoBanco.getIdPromocao()).isEqualTo(idPromocao);
        assertThat(promoDoBanco.getDescricao()).isEqualTo("Happy Hour Teste");
        assertThat(promoDoBanco.getDesconto()).isEqualTo(15.0);
        assertThat(promoDoBanco.isAtiva()).isTrue();
        assertThat(promoDoBanco.getTotalCliques()).isEqualTo(0);

        assertThat(promoDoBanco.getEstabelecimento()).isNotNull();
        assertThat(promoDoBanco.getEstabelecimento().getIdEstabelecimento()).isEqualTo(estSalvo.getIdEstabelecimento());
        assertThat(promoDoBanco.getCupom()).isNotNull();
        assertThat(promoDoBanco.getCupom().getIdCupom()).isEqualTo(cupomSalvo.getIdCupom());
    }

    @Test
    @DisplayName("Deve persistir promocao sem cupom")
    void devePersistirPromocaoSemCupom() {

        Estabelecimento est = new Estabelecimento();
        est.setNome("Restaurante Teste");
        est.setEmail("rest@teste.com");
        est.setSenha("123");
        Estabelecimento estSalvo = testEntityManager.persist(est);

        Promocao promocao = Promocao.builder()
                .estabelecimento(estSalvo)
                .cupom(null)
                .descricao("Promoção Sem Cupom")
                .desconto(5.0)
                .build();

        Promocao promoSalva = testEntityManager.persistAndFlush(promocao);
        Long idPromocao = promoSalva.getIdPromocao();
        testEntityManager.clear();

        Promocao promoDoBanco = testEntityManager.find(Promocao.class, idPromocao);

        assertThat(promoDoBanco).isNotNull();
        assertThat(promoDoBanco.getDescricao()).isEqualTo("Promoção Sem Cupom");
        assertThat(promoDoBanco.getCupom()).isNull();
        assertThat(promoDoBanco.getEstabelecimento()).isNotNull();
        assertThat(promoDoBanco.getEstabelecimento().getNome()).isEqualTo("Restaurante Teste");
    }
}