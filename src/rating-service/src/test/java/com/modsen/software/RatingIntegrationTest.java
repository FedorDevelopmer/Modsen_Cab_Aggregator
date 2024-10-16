package com.modsen.software;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsen.software.rating.RatingServiceApplication;
import com.modsen.software.rating.dto.DriverResponseTO;
import com.modsen.software.rating.dto.PassengerResponseTO;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.repository.RatingRepository;
import com.modsen.software.rating.service.impl.RatingServiceImpl;
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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = RatingServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RatingIntegrationTest {

    @LocalServerPort
    private Integer port;

    private static final String URI = "/api/v1/scores";

    private static final String TABLE_NAME = "rating_scores";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static WireMockServer driverServiceMock = new WireMockServer(options().port(8080));

    private static WireMockServer passengerServiceMock = new WireMockServer(options().port(8081));

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RatingServiceImpl ratingService;

    private static RatingScore ratingScore;

    private static RatingScore secondRatingScore;

    @Autowired
    private ObjectMapper mapper;

    @BeforeAll
    static void beforeAll() throws Exception {
        postgres.start();

        ratingScore = RatingScore.builder()
                .id(1L)
                .driverId(1L)
                .passengerId(1L)
                .comment("Excellent ride!")
                .evaluation(5)
                .initiator(Initiator.PASSENGER)
                .build();

        secondRatingScore = RatingScore.builder()
                .id(2L)
                .driverId(2L)
                .passengerId(2L)
                .comment("Passenger was polite and communicative.")
                .evaluation(4)
                .initiator(Initiator.DRIVER)
                .build();

        ObjectMapper wireMockMapper = new ObjectMapper();
        driverServiceMock.stubFor(WireMock.get(WireMock.anyUrl()).willReturn(WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(wireMockMapper.writeValueAsString(new DriverResponseTO()))));
        passengerServiceMock.stubFor(WireMock.get(WireMock.anyUrl()).willReturn(WireMock.aResponse()
                .withHeader("Content-Type", "application/json")
                .withBody(wireMockMapper.writeValueAsString(new PassengerResponseTO()))));
        driverServiceMock.start();
        passengerServiceMock.start();
    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
        driverServiceMock.stop();
        passengerServiceMock.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", () -> "localhost:9092");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/changelog_root.xml");
    }

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        jdbcTemplate.execute("TRUNCATE TABLE " + TABLE_NAME + " RESTART IDENTITY");
    }

    @Test
    void testGetAllRatingScores() throws Exception {
        saveRating(ratingScore);
        saveRating(secondRatingScore);
        mockMvc.perform(get(URI)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].driverId", is(1)))
                .andExpect(jsonPath("$.content[0].passengerId", is(1)))
                .andExpect(jsonPath("$.content[0].comment", is("Excellent ride!")))
                .andExpect(jsonPath("$.content[0].evaluation", is(5)))
                .andExpect(jsonPath("$.content[1].driverId", is(2)))
                .andExpect(jsonPath("$.content[1].passengerId", is(2)))
                .andExpect(jsonPath("$.content[1].comment", is("Passenger was polite and communicative.")))
                .andExpect(jsonPath("$.content[1].evaluation", is(4)));
    }

    @Test
    void testGetAllRatingScoresWithFilter() throws Exception {
        saveRating(ratingScore);
        saveRating(secondRatingScore);
        mockMvc.perform(get(URI)
                        .param("initiator", Initiator.DRIVER.name())
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].driverId", is(2)))
                .andExpect(jsonPath("$.content[0].passengerId", is(2)))
                .andExpect(jsonPath("$.content[0].comment", is("Passenger was polite and communicative.")))
                .andExpect(jsonPath("$.content[0].evaluation", is(4)));
    }

    @Test
    void testSaveRatingScore() throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(ratingScore))
                        .accept("application/json"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId", is(1)))
                .andExpect(jsonPath("$.passengerId", is(1)))
                .andExpect(jsonPath("$.comment", is("Excellent ride!")))
                .andExpect(jsonPath("$.evaluation", is(5)))
                .andExpect(jsonPath("$.initiator", is("PASSENGER")));
    }

    @Test
    void testUpdateRatingScore() throws Exception {
        saveRating(ratingScore);
        RatingScore ratingToUpdate = RatingScore.builder()
                .id(ratingScore.getId())
                .driverId(ratingScore.getDriverId())
                .passengerId(ratingScore.getPassengerId())
                .evaluation(ratingScore.getEvaluation())
                .comment(ratingScore.getComment())
                .initiator(ratingScore.getInitiator())
                .build();

        ratingToUpdate.setComment("Not so bad ride!");
        ratingToUpdate.setEvaluation(4);
        mockMvc.perform(put(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(ratingToUpdate))
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment", is("Not so bad ride!")))
                .andExpect(jsonPath("$.evaluation", is(4)));
    }

    @Test
    void testDeleteRatingScore() throws Exception {
        saveRating(ratingScore);
        mockMvc.perform(delete(URI + "/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void testFindRatingScoreById() throws Exception {
        saveRating(ratingScore);
        mockMvc.perform(get(URI + "/{id}", 1L)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.driverId", is(1)))
                .andExpect(jsonPath("$.passengerId", is(1)))
                .andExpect(jsonPath("$.comment", is("Excellent ride!")))
                .andExpect(jsonPath("$.evaluation", is(5)));
    }

    private void saveRating(RatingScore rating) throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(rating))
                        .accept("application/json"))
                .andExpect(status().isCreated());
    }
}
