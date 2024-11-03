package com.modsen.software.driver.component_testing.driver_test;

import com.modsen.software.driver.DriverServiceApplication;
import io.cucumber.spring.CucumberContextConfiguration;
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
@SpringBootTest(classes = DriverServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DriverTestLoader {

    public static final String URI = "/api/v1/drivers";

    public static final String DRIVERS_TABLE_NAME = "drivers";

    public static final long MONTH_DURATION = 86_400_000 * 30L;

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
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/test/test_changelog_root.xml");
    }
}

