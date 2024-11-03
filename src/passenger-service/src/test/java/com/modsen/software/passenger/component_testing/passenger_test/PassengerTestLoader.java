package com.modsen.software.passenger.component_testing.passenger_test;

import com.modsen.software.passenger.PassengerServiceApplication;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@CucumberContextConfiguration
@AutoConfigureMockMvc
@Testcontainers
@ExtendWith(MockitoExtension.class)
@SpringBootTest(classes = PassengerServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PassengerTestLoader {

    public static final String URI = "/api/v1/passengers";

    public static final String PASSENGERS_TABLE_NAME = "passengers";

    @LocalServerPort
    private Integer port;

    @Container
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:latest");

    static {
        container.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", container::getJdbcUrl);
        registry.add("spring.datasource.username", container::getUsername);
        registry.add("spring.datasource.password", container::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/changelog_root.xml");
    }
}

