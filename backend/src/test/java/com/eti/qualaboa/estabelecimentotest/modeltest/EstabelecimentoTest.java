package com.eti.qualaboa.estabelecimentotest.modeltest;

import com.eti.qualaboa.endereco.Endereco;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.usuario.domain.entity.Role;
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

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = EstabelecimentoTest.Initializer.class)
@TestPropertySource(properties = "spring.sql.init.mode=never")
@EntityScan(basePackages = "com.eti.qualaboa")
public class EstabelecimentoTest {

    @Autowired
    private TestEntityManager testEntityManager;

    private Role roleEstabelecimento;


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
        Role role = new Role();
        role.setNome("ESTABELECIMENTO");
        this.roleEstabelecimento = testEntityManager.persist(role);
        testEntityManager.clear();
    }

    @Test
    @DisplayName("Deve persistir um Estabelecimento com todos os relacionamentos")
    void devePersistirEstabelecimentoCompleto() {

        Endereco endereco = Endereco.builder()
                .rua("Rua Teste")
                .numero("123")
                .cidade("Maringá")
                .cep("87000-000")
                .build();

        byte[] imagemBytes = "imagem_fake".getBytes(StandardCharsets.UTF_8);

        List<String> conveniencias = List.of("wifi", "musica ao vivo");

        Estabelecimento est = Estabelecimento.builder()
                .nome("Bar do Teste")
                .email("bar@teste.com")
                .senha("senha123")
                .categoria("bar")
                .descricao("Descrição de teste")
                .telefone("4499999999")
                .classificacao(4.5)
                .parceiro(true)
                .placeId("place123")
                .latitude(-23.42)
                .longitude(-51.93)
                .enderecoFormatado("Rua Teste, 123, Maringá")
                .endereco(endereco)
                .fotoUrl( "imagem_fake")
                .conveniencias(conveniencias)
                .roles(Set.of(roleEstabelecimento))
                .build();

        Estabelecimento estSalvo = testEntityManager.persistAndFlush(est);
        Long idSalvo = estSalvo.getIdEstabelecimento();
        Long idEnderecoSalvo = estSalvo.getEndereco().getIdEndereco();

        testEntityManager.clear();

        Estabelecimento estDoBanco = testEntityManager.find(Estabelecimento.class, idSalvo);

        assertThat(estDoBanco).isNotNull();
        assertThat(estDoBanco.getNome()).isEqualTo("Bar do Teste");
        assertThat(estDoBanco.getClassificacao()).isEqualTo(4.5);
        assertThat(new String(estDoBanco.getFotoUrl())).isEqualTo("imagem_fake");

        assertThat(estDoBanco.getEndereco()).isNotNull();
        assertThat(estDoBanco.getEndereco().getIdEndereco()).isEqualTo(idEnderecoSalvo);
        assertThat(estDoBanco.getEndereco().getCidade()).isEqualTo("Maringá");

        assertThat(estDoBanco.getConveniencias()).isNotNull().hasSize(2);
        assertThat(estDoBanco.getConveniencias()).containsExactlyInAnyOrder("wifi", "musica ao vivo");

        assertThat(estDoBanco.getRoles()).isNotNull().hasSize(1);
        assertThat(estDoBanco.getRoles().iterator().next().getNome()).isEqualTo("ESTABELECIMENTO");
    }
}