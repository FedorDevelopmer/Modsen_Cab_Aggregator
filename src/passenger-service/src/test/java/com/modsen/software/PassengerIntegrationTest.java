package com.modsen.software;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsen.software.passenger.PassengerServiceApplication;
import com.modsen.software.passenger.dto.RatingEvaluationResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.repository.PassengerRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = PassengerServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PassengerIntegrationTest {

    @LocalServerPort
    private Integer port;

    private static final String TABLE_NAME = "passengers";

    private static final String URI = "/api/v1/passengers";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    private static WireMockServer ratingServiceMock = new WireMockServer(options().port(8083));

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static Passenger passenger;

    private static Passenger secondPassenger;

    private ObjectMapper mapper = new ObjectMapper();

    @BeforeAll
    static void beforeAll() throws Exception {
        postgres.start();
        passenger = Passenger.builder()
                .id(1L)
                .name("John")
                .email("john.con@gmail.com")
                .gender(Gender.MALE)
                .phoneNumber("+745-432-143")
                .rating(BigDecimal.valueOf(5))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        secondPassenger = Passenger.builder()
                .id(2L)
                .name("Laila")
                .email("lai.tess@gmail.com")
                .gender(Gender.FEMALE)
                .phoneNumber("+334-332-986")
                .rating(BigDecimal.valueOf(5))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        ObjectMapper wireMockMapper = new ObjectMapper();
        ratingServiceMock.stubFor(WireMock.get(WireMock.anyUrl()).willReturn(WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(wireMockMapper.writeValueAsString(new RatingEvaluationResponseTO()))));
        ratingServiceMock.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        ratingServiceMock.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/changelog_root.xml");
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE " + TABLE_NAME + " RESTART IDENTITY");
        passengerRepository.deleteAll();
    }

    @Test
    @Transactional
    void testGetAllPassengers() throws Exception {
        List<Passenger> passengers = List.of(passenger, secondPassenger);
        passengerRepository.saveAll(passengers);
        mockMvc.perform(get(URI)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].name", is("John")))
                .andExpect(jsonPath("$.content[0].email", is("john.con@gmail.com")))
                .andExpect(jsonPath("$.content[0].phoneNumber", is("+745-432-143")))
                .andExpect(jsonPath("$.content[1].name", is("Laila")))
                .andExpect(jsonPath("$.content[1].email", is("lai.tess@gmail.com")))
                .andExpect(jsonPath("$.content[1].phoneNumber", is("+334-332-986")));
    }

    @Test
    @Transactional
    void testGetAllPassengersWithFilter() throws Exception {
        List<Passenger> passengers = List.of(passenger, secondPassenger);
        passengerRepository.saveAll(passengers);
        mockMvc.perform(get(URI)
                        .param("gender", Gender.FEMALE.name())
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("Laila")))
                .andExpect(jsonPath("$.content[0].email", is("lai.tess@gmail.com")))
                .andExpect(jsonPath("$.content[0].phoneNumber", is("+334-332-986")));
    }

    @Test
    @Transactional
    void testSavePassenger() throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(secondPassenger))
                        .accept("application/json"))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.name", is("Laila")))
                .andExpect(jsonPath("$.email", is("lai.tess@gmail.com")))
                .andExpect(jsonPath("$.phoneNumber", is("+334-332-986")));
    }

    @Test
    @Transactional
    void testUpdatePassenger() throws Exception {
        savePassenger(passenger);
        Passenger passengerToUpdate = passenger;
        passengerToUpdate.setName("Nikolas");
        passengerToUpdate.setPhoneNumber("+343-234-000");
        mockMvc.perform(put(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(passengerToUpdate))
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.name", is("Nikolas")))
                .andExpect(jsonPath("$.phoneNumber", is("+343-234-000")));
    }

    @Test
    @Transactional
    void testDeletePassenger() throws Exception {
        savePassenger(passenger);
        mockMvc.perform(delete("http://localhost:" + port + URI + "/{id}", 1L))
                .andExpect(status().is(204));
    }

    @Test
    @Transactional
    void testFindPassengerById() throws Exception {
        savePassenger(secondPassenger);
        mockMvc.perform(get(URI + "/{id}", 1L)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("Laila")))
                .andExpect(jsonPath("$.email", is("lai.tess@gmail.com")));
    }

    private void savePassenger(Passenger passenger) throws Exception {
        mockMvc.perform(post(URI)
                .contentType("application/json")
                .content(mapper.writeValueAsString(passenger))
                .accept("application/json"));
    }
}
