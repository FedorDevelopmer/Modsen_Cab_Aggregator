package com.modsen.software.rating.component_testing.rating_test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsen.software.rating.RatingServiceApplication;
import com.modsen.software.rating.dto.DriverResponseTO;
import com.modsen.software.rating.dto.PassengerResponseTO;
import io.cucumber.spring.CucumberContextConfiguration;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@CucumberContextConfiguration
@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(classes = RatingServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RatingTestLoader {

    public static final String URI = "/api/v1/scores";

    public static final String RATING_SCORES_TABLE_NAME = "rating_scores";

    private static WireMockServer driverServiceMock = new WireMockServer(options().port(8080));

    private static WireMockServer passengerServiceMock = new WireMockServer(options().port(8081));

    @LocalServerPort
    private Integer port;

    @Container
    static PostgreSQLContainer<?> container = new PostgreSQLContainer<>("postgres:latest");

    static {
        try {
            container.start();
            ObjectMapper wireMockMapper = new ObjectMapper();
            driverServiceMock.stubFor(WireMock.get(WireMock.anyUrl()).willReturn(WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(wireMockMapper.writeValueAsString(new DriverResponseTO()))));

            passengerServiceMock.stubFor(WireMock.get(WireMock.anyUrl()).willReturn(WireMock.aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withBody(wireMockMapper.writeValueAsString(new PassengerResponseTO()))));
            driverServiceMock.start();
            passengerServiceMock.start();
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
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

