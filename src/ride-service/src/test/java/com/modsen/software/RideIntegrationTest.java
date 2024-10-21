package com.modsen.software;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsen.software.ride.RideServiceApplication;
import com.modsen.software.ride.dto.DriverResponseTO;
import com.modsen.software.ride.dto.PassengerResponseTO;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.repository.RideRepository;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = RideServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RideIntegrationTest {

    @LocalServerPort
    private Integer port;

    private static final String URI = "/api/v1/rides";

    private static final String TABLE_NAME = "rides";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static WireMockServer driverServiceMock = new WireMockServer(options().port(8080));

    private static WireMockServer passengerServiceMock = new WireMockServer(options().port(8081));

    @Autowired
    private RideRepository rideRepository;

    private static Ride ride;

    private static Ride secondRide;

    @Autowired
    private ObjectMapper mapper;

    @BeforeAll
    static void beforeAll() throws Exception {

        postgres.start();

        ride = Ride.builder()
                .id(1L)
                .driverId(1L)
                .passengerId(1L)
                .departureAddress("Minsk,Vaneeva,8")
                .destinationAddress("Minsk,Gintovta,30")
                .ridePrice(BigDecimal.valueOf(35))
                .rideStatus(RideStatus.CREATED)
                .rideOrderTime(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))))
                .build();

        secondRide = Ride.builder()
                .id(2L)
                .driverId(2L)
                .passengerId(2L)
                .departureAddress("Minsk,Bricketa,8")
                .destinationAddress("Minsk,Dombrouskaya,30")
                .ridePrice(BigDecimal.valueOf(25))
                .rideStatus(RideStatus.ACCEPTED)
                .rideOrderTime(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))))
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
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create");
        registry.add("spring.liquibase.enabled", () -> "true");
        registry.add("spring.liquibase.change-log", () -> "classpath:db/changelog/changelog_root.xml");
    }

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        jdbcTemplate.execute("TRUNCATE TABLE " + TABLE_NAME + " RESTART IDENTITY");
    }

    @Test
    void testGetAllRides() throws Exception {
        saveRide(ride);
        saveRide(secondRide);
        mockMvc.perform(get(URI)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].destinationAddress", is("Minsk,Gintovta,30")))
                .andExpect(jsonPath("$.content[0].rideStatus", is("CREATED")))
                .andExpect(jsonPath("$.content[0].ridePrice", is(ride.getRidePrice().doubleValue())))
                .andExpect(jsonPath("$.content[1].destinationAddress", is("Minsk,Dombrouskaya,30")))
                .andExpect(jsonPath("$.content[1].rideStatus", is("ACCEPTED")))
                .andExpect(jsonPath("$.content[1].ridePrice", is(BigDecimal.valueOf(25).doubleValue())));
    }

    @Test
    void testGetAllRidesWithFilter() throws Exception {
        saveRide(ride);
        saveRide(secondRide);
        mockMvc.perform(get(URI)
                        .param("ridePrice", BigDecimal.valueOf(25).toString())
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].destinationAddress", is("Minsk,Dombrouskaya,30")))
                .andExpect(jsonPath("$.content[0].rideStatus", is("ACCEPTED"))));
                .andExpect(jsonPath("$.content[0].ridePrice", is(BigDecimal.valueOf(25).doubleValue())));
    }

    @Test
    void testSaveRide() throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(ride))
                        .accept("application/json"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.driverId", is(1)))
                .andExpect(jsonPath("$.passengerId", is(1)))
                .andExpect(jsonPath("$.departureAddress", is("Minsk,Vaneeva,8")))
                .andExpect(jsonPath("$.destinationAddress", is("Minsk,Gintovta,30")))
                .andExpect(jsonPath("$.ridePrice", is(BigDecimal.valueOf(35).intValue())));
    }

    @Test
    void testUpdateRide() throws Exception {
        saveRide(ride);
        Ride rideToUpdate = ride;
        rideToUpdate.setDepartureAddress("Minsk,Gusouskoga,9");
        rideToUpdate.setRidePrice(BigDecimal.valueOf(36));
        mockMvc.perform(put(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(rideToUpdate))
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departureAddress", is("Minsk,Gusouskoga,9")))
                .andExpect(jsonPath("$.ridePrice", is(BigDecimal.valueOf(36).intValue())));
    }

    @Test
    void testDeleteRide() throws Exception {
        saveRide(ride);
        mockMvc.perform(delete(URI + "/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void testFindRideById() throws Exception {
        saveRide(secondRide);
        mockMvc.perform(get(URI + "/{id}", 1L)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.destinationAddress", is("Minsk,Dombrouskaya,30")))
                .andExpect(jsonPath("$.departureAddress", is("Minsk,Bricketa,8")))
                .andExpect(jsonPath("$.driverId", is(2)));
    }

    private void saveRide(Ride ride) throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(ride))
                        .accept("application/json"))
                .andExpect(status().isCreated());
    }
}
