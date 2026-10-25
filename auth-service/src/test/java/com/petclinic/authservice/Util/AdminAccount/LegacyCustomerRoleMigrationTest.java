package com.petclinic.authservice.Util.AdminAccount;

import com.petclinic.authservice.datalayer.roles.RoleRepo;
import com.petclinic.authservice.datalayer.user.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.support.GenericApplicationContext;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@DataJpaTest(properties = {"spring.datasource.url=jdbc:h2:mem:role-migration;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop", "spring.sql.init.mode=never"})
@Import(LegacyCustomerRoleMigration.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class LegacyCustomerRoleMigrationTest {
    @Autowired RoleRepo roles;
    @Autowired UserRepo users;
    @Autowired JdbcTemplate jdbc;
    @Autowired LegacyCustomerRoleMigration migration;
    DatabaseLoaderService loader;

    @BeforeEach
    void reset() {
        jdbc.update("delete from users_roles");
        jdbc.update("delete from users");
        jdbc.update("delete from roles");
        jdbc.execute("alter table roles alter column id restart with 1");
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.encode(any())).thenReturn("encoded-password");
        loader = new DatabaseLoaderService(users, encoder, roles, migration);
    }

    private void populated(String name) {
        jdbc.update("insert into roles(id,name) values(42, ?), (77, 'VET')", name);
        jdbc.update("insert into users(id,username,password,email,user_id,verified,is_disabled) "
                + "values(91,'existing','hash','existing@example.com','stable-account-id',true,false),"
                + "(92,'second','hash2','second@example.com','second-account-id',true,false)");
        jdbc.update("insert into users_roles(user_id,role_id) values(91,42),(92,42),(91,77)");
    }

    private List<Map<String, Object>> snapshot(String table, String order) {
        return jdbc.queryForList("select * from " + table + " order by " + order);
    }

    @Test
    void populatedOwnerPreservesExactMembershipsAccountsAndOtherRolesAndCanRepeat() {
        populated("OWNER");
        var accounts = snapshot("users", "id");
        var memberships = snapshot("users_roles", "user_id,role_id");
        loader.run();
        assertThat(roles.findRoleByName("CUST").getId()).isEqualTo(42);
        assertThat(roles.findRoleByName("OWNER")).isNull();
        assertThat(roles.findRoleByName("VET").getId()).isEqualTo(77);
        assertThat(roles.count()).isEqualTo(2);
        assertThat(snapshot("users", "id")).isEqualTo(accounts);
        assertThat(snapshot("users_roles", "user_id,role_id")).isEqualTo(memberships);
        var migratedRoles = snapshot("roles", "id");
        loader.run();
        assertThat(snapshot("roles", "id")).isEqualTo(migratedRoles);
        assertThat(snapshot("users", "id")).isEqualTo(accounts);
        assertThat(snapshot("users_roles", "user_id,role_id")).isEqualTo(memberships);
    }

    @Test
    void alreadyCustomerIsNoOp() {
        populated("CUST");
        assertUnchangedAfterStartup();
    }

    private void assertUnchangedAfterStartup() {
        var roleRows = snapshot("roles", "id");
        var accounts = snapshot("users", "id");
        var memberships = snapshot("users_roles", "user_id,role_id");
        loader.run();
        assertThat(snapshot("roles", "id")).isEqualTo(roleRows);
        assertThat(snapshot("users", "id")).isEqualTo(accounts);
        assertThat(snapshot("users_roles", "user_id,role_id")).isEqualTo(memberships);
    }

    @Test
    void neitherRoleDoesNotResetExistingData() {
        populated("ADMIN");
        assertUnchangedAfterStartup();
    }

    @Test
    void freshDatabaseSeedsCustomer() {
        loader.run();
        assertThat(roles.findRoleByName("CUST")).isNotNull();
        assertThat(roles.findRoleByName("OWNER")).isNull();
        assertThat(roles.count()).isEqualTo(5);
        assertThat(users.count()).isEqualTo(20);
    }

    @Test
    void bothRolesFailWithoutChangingData() {
        populated("OWNER");
        jdbc.update("insert into roles(id,name) values(43,'CUST')");
        var roleRows = snapshot("roles", "id");
        var accounts = snapshot("users", "id");
        var memberships = snapshot("users_roles", "user_id,role_id");
        assertThatThrownBy(() -> loader.run()).isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("OWNER and CUST both exist")
                .hasMessageContaining("database operator");
        assertThat(snapshot("roles", "id")).isEqualTo(roleRows);
        assertThat(snapshot("users", "id")).isEqualTo(accounts);
        assertThat(snapshot("users_roles", "user_id,role_id")).isEqualTo(memberships);
    }

    @Test
    void concurrentPopulatedStartupsAreSafe() throws Exception {
        populated("OWNER");
        var memberships = snapshot("users_roles", "user_id,role_id");
        var executor = Executors.newFixedThreadPool(2);
        var start = new CountDownLatch(1);
        try {
            Callable<Void> startup = () -> {
                start.await();
                loader.run();
                return null;
            };
            var first = executor.submit(startup);
            var second = executor.submit(startup);
            start.countDown();
            first.get(15, TimeUnit.SECONDS);
            second.get(15, TimeUnit.SECONDS);
            assertThat(roles.findRoleByName("CUST").getId()).isEqualTo(42);
            assertThat(roles.findRoleByName("OWNER")).isNull();
            assertThat(snapshot("users_roles", "user_id,role_id")).isEqualTo(memberships);
        } finally {
            executor.shutdownNow();
        }
    }

    static class StartupConfiguration { }

    @Test
    void persistenceFailureRollsBackAndEscapesStartupRunner() {
        populated("OWNER");
        jdbc.execute("alter table roles add constraint reject_customer check(name <> 'CUST')");
        var roleRows = snapshot("roles", "id");
        var accounts = snapshot("users", "id");
        var memberships = snapshot("users_roles", "user_id,role_id");
        try {
            var ready = new AtomicBoolean();
            var application = new SpringApplication(StartupConfiguration.class);
            application.setWebApplicationType(WebApplicationType.NONE);
            application.addInitializers(context ->
                    ((GenericApplicationContext) context)
                            .registerBean("databaseLoader", DatabaseLoaderService.class, () -> loader));
            application.addListeners((ApplicationListener<ApplicationReadyEvent>)
                    event -> ready.set(true));
            assertThatThrownBy(() -> application.run("--spring.main.banner-mode=off"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasRootCauseInstanceOf(JdbcSQLIntegrityConstraintViolationException.class);
            assertThat(ready).isFalse();
            assertThat(snapshot("roles", "id")).isEqualTo(roleRows);
            assertThat(snapshot("users", "id")).isEqualTo(accounts);
            assertThat(snapshot("users_roles", "user_id,role_id")).isEqualTo(memberships);
        } finally {
            jdbc.execute("alter table roles drop constraint reject_customer");
        }
    }
}
