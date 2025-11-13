package com.eti.qualaboa.usuariotest.domain.entitytest;

import com.eti.qualaboa.enums.Sexo;
import com.eti.qualaboa.estabelecimento.model.Estabelecimento;
import com.eti.qualaboa.usuario.domain.entity.Role;
import com.eti.qualaboa.usuario.domain.entity.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@ContextConfiguration(initializers = UsuarioTest.Initializer.class)
public class UsuarioTest {

    @Container
    private static final PostgreSQLContainer<?> postgresContainer;

    // Configuração do Testcontainers (exatamente igual ao RoleTest)
    static {
        DockerImageName postgisImage = DockerImageName.parse("postgis/postgis:15-3.3")
                .asCompatibleSubstituteFor("postgres");

        postgresContainer = new PostgreSQLContainer<>(postgisImage)
                .withDatabaseName("qualaboa")
                .withUsername("qualaboa_user")
                .withPassword("senha123");

        postgresContainer.start();
    }

    // Initializer (exatamente igual ao RoleTest)
    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {
            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "spring.datasource.url=" + postgresContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresContainer.getUsername(),
                    "spring.datasource.password=" + postgresContainer.getPassword(),
                    "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect",
                    "spring.jpa.hibernate.ddl-auto=create-drop",
                    "spring.sql.init.mode=never" // Banco de dados limpo
            );
        }
    }

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("Deve persistir Usuario com Roles, Preferencias e Favoritos")
    void devePersistirUsuarioCompleto() {
        // --- ARRANGE ---

        // 1. Criar dependência: Role
        Role userRole = new Role();
        userRole.setNome("USER");
        Role persistedRole = testEntityManager.persistFlushFind(userRole);

        // 2. Criar dependência: Estabelecimento
        Estabelecimento bar = new Estabelecimento();
        bar.setNome("Bar Favorito");
        bar.setEmail("bar@email.com");
        bar.setSenha("123"); // Campos obrigatórios da entidade
        Estabelecimento persistedBar = testEntityManager.persistFlushFind(bar);

        // 3. Criar o Usuario
        Usuario user = new Usuario();
        user.setNome("Test User");
        user.setEmail("test@user.com");
        user.setSenha("senha123");
        user.setSexo(Sexo.MASCULINO);
        user.setPreferenciasUsuario(List.of("Rock", "Pagode"));
        user.setRoles(Set.of(persistedRole));
        user.setFavoritos(Set.of(persistedBar));

        // --- ACT ---
        // Salva o usuário e limpa o cache
        Usuario savedUser = testEntityManager.persistAndFlush(user);
        Long savedId = savedUser.getId();
        testEntityManager.clear();

        // --- ASSERT ---
        // Busca o usuário do banco de dados
        Usuario foundUser = testEntityManager.find(Usuario.class, savedId);

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(savedId);
        assertThat(foundUser.getNome()).isEqualTo("Test User");
        assertThat(foundUser.getEmail()).isEqualTo("test@user.com");
        assertThat(foundUser.getSexo()).isEqualTo(Sexo.MASCULINO);

        // Verifica coleções
        assertThat(foundUser.getPreferenciasUsuario()).containsExactlyInAnyOrder("Rock", "Pagode");

        // Verifica relacionamento EAGER (Roles)
        assertThat(foundUser.getRoles()).hasSize(1);
        assertThat(foundUser.getRoles().iterator().next().getNome()).isEqualTo("USER");

        // Verifica relacionamento LAZY (Favoritos)
        // Ao acessar .getFavoritos(), o Hibernate fará a busca
        assertThat(foundUser.getFavoritos()).hasSize(1);
        assertThat(foundUser.getFavoritos().iterator().next().getNome()).isEqualTo("Bar Favorito");
    }
}