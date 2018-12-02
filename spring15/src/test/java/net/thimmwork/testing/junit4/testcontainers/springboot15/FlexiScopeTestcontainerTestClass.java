package net.thimmwork.testing.junit4.testcontainers.springboot15;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.persistence.Entity;
import java.util.Properties;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@SpringBootTest
@SpringBootConfiguration
@RunWith(SpringRunner.class)
public class FlexiScopeTestcontainerTestClass {

    //note that each test class in the suite uses the same instance of the semaphore instantiated by the suite class
    //this way, beforeSuite() and beforeClass() are invoked on the same instance.
    @ClassRule @Rule
    public static PostgreSQLContainer POSTGRES = FlexiScopeTestcontainerSuiteTest.POSTGRES;

    @ClassRule @Rule
    public static KafkaContainer KAFKA = FlexiScopeTestcontainerSuiteTest.KAFKA;

    @Autowired
    private Repo repo;

    @Test
    public void setUp_is_called_only_once_in_test_suite() {
        TestEntity testEntity = new TestEntity();
        testEntity.id = UUID.randomUUID();

        repo.save(testEntity);
        TestEntity persisted = repo.findOne(testEntity.id);

        assertEquals(testEntity.id, persisted.id);
    }

    @TestConfiguration
    public static class TestConfig {
        @Bean
        public PropertySourcesPlaceholderConfigurer config() {
            final PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
            Properties properties = new Properties();

            properties.setProperty("spring.datasource.url", POSTGRES.getJdbcUrl());
            properties.setProperty("spring.datasource.username", POSTGRES.getUsername());
            properties.setProperty("spring.datasource.password", POSTGRES.getPassword());

            properties.setProperty("spring.kafka.bootstrap-servers", KAFKA.getBootstrapServers().replace("PLAINTEXT://", ""));

            pspc.setProperties(properties);
            pspc.setLocalOverride(true); //this will give our properties higher priority than application.yml
            return pspc;
        }
    }

    @Entity
    public static class TestEntity {
        private UUID id;
    }

    @Repository
    public interface Repo extends CrudRepository<TestEntity, UUID> {}

}
