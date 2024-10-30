package com.modsen.software.ride.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.ride.RideServiceApplication;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.repository.RideRepository;
import com.modsen.software.ride.service.impl.RideServiceImpl;
import io.restassured.RestAssured;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(classes = RideServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RideServiceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    private static final String TABLE_NAME = "rides";

    private static String URI = "/api/v1/rides";

    private static String oldJdbcUrl = "";

    private static String oldKafkaBootstrapServers = "";

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private RideServiceImpl rideService;

    private static Ride ride;

    private static Ride secondRide;

    private static String driverJson;

    private static String passengerJson;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper mapper;

    static Network network = Network.newNetwork();

    @Container
    private static PostgreSQLContainer<?> rideServiceDB = new PostgreSQLContainer<>("postgres:latest")
            .withNetwork(network)
            .withExposedPorts(5432)
            .withUsername("user")
            .withPassword("UltraStrongPassw0rd");

    @Container
    private static PostgreSQLContainer<?> driverServiceDB = new PostgreSQLContainer<>("postgres:latest")
            .withNetwork(network)
            .withExposedPorts(5432)
            .withUsername("user")
            .withPassword("UltraStrongPassw0rd");

    @Container
    private static PostgreSQLContainer<?> passengerServiceDB = new PostgreSQLContainer<>("postgres:latest")
            .withNetwork(network)
            .withExposedPorts(5432)
            .withUsername("user")
            .withPassword("UltraStrongPassw0rd");

    private static GenericContainer<?> driverService;

    private static GenericContainer<?> passengerService;

    @Container
    private static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withNetwork(network)
            .withExposedPorts(9092, 9093);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", rideServiceDB::getJdbcUrl);
        registry.add("spring.datasource.username", rideServiceDB::getUsername);
        registry.add("spring.datasource.password", rideServiceDB::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        String driverUrl = "http://localhost:" + driverService.getFirstMappedPort() + "/api/v1/drivers";
        String passengerUrl = "http://localhost:" + passengerService.getFirstMappedPort() + "/api/v1/passengers";
        registry.add("ride-service.drivers.url", () -> driverUrl);
        registry.add("ride-service.passengers.url", () -> passengerUrl);
    }

    @BeforeAll
    static void beforeAll() {
        rideServiceDB.start();
        driverServiceDB.start();
        driverService = new GenericContainer<>(DockerImageName.parse("driver_service"))
                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://host.docker.internal:" + driverServiceDB.getMappedPort(5432) + "/test")
                .withEnv("SPRING_DATASOURCE_USERNAME", "user")
                .withEnv("SPRING_DATASOURCE_PASSWORD", "UltraStrongPassw0rd")
                .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "host.docker.internal:" + kafka.getMappedPort(9092))
                .withExposedPorts(8080)
                .withNetwork(network);
        passengerService = new GenericContainer<>(DockerImageName.parse("passenger_service"))
                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://host.docker.internal:" + passengerServiceDB.getMappedPort(5432) + "/test")
                .withEnv("SPRING_DATASOURCE_USERNAME", "user")
                .withEnv("SPRING_DATASOURCE_PASSWORD", "UltraStrongPassw0rd")
                .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "host.docker.internal:" + kafka.getMappedPort(9092))
                .withExposedPorts(8081)
                .withNetwork(network);
        passengerServiceDB.start();
        driverService.start();
        passengerService.start();

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
                .driverId(1L)
                .passengerId(1L)
                .departureAddress("Minsk,Bricketa,8")
                .destinationAddress("Minsk,Dombrouskaya,30")
                .ridePrice(BigDecimal.valueOf(25))
                .rideStatus(RideStatus.ACCEPTED)
                .rideOrderTime(LocalDateTime.parse(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'hh:mm:ss"))))
                .build();

        driverJson = "{" +
                "    \"id\": 1,\n" +
                "    \"name\": \"John\",\n" +
                "    \"surname\": \"Doe\",\n" +
                "    \"email\": \"john.doe@example.com\",\n" +
                "    \"phoneNumber\": \"+434-422-780\",\n" +
                "    \"rating\": 4.5,\n" +
                "    \"gender\": \"MALE\",\n" +
                "    \"birthDate\": \"1990-01-01\",\n" +
                "    \"ratingUpdateTimestamp\": \"2023-10-05T12:34:56.789\",\n" +
                "    \"removeStatus\": \"ACTIVE\",\n" +
                "    \"cars\": []\n" +
                "}";

        passengerJson = "{" +
                "    \"id\": 1,\n" +
                "    \"name\": \"John Doe\",\n" +
                "    \"email\": \"john.doe@example.com\",\n" +
                "    \"phoneNumber\": \"003-226-710\",\n" +
                "    \"rating\": 4.5,\n" +
                "    \"gender\": \"MALE\",\n" +
                "    \"ratingUpdateTimestamp\": \"2023-10-05T12:34:56\",\n" +
                "    \"removeStatus\": \"ACTIVE\"\n" +
                "}";
        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(driverJson)
                .post("http://localhost:" + driverService.getFirstMappedPort() + "/api/v1/drivers")
                .then()
                .statusCode(201);
        RestAssured.given()
                .header("Content-Type", "application/json")
                .body(passengerJson)
                .post("http://localhost:" + passengerService.getFirstMappedPort() + "/api/v1/passengers")
                .then()
                .statusCode(201);

    }

    @AfterAll
    static void afterAll() {
        rideServiceDB.stop();
        driverServiceDB.stop();
        driverService.stop();
        passengerService.stop();
        passengerServiceDB.stop();
        kafka.stop();
        System.setProperty("spring.datasource.url", oldJdbcUrl);
        System.setProperty("spring.kafka.bootstrap-servers", oldKafkaBootstrapServers);
        System.setProperty("ride-service.drivers.url", "http://localhost:8080/api/v1/drivers");
        System.setProperty("ride-service.passengers.url", "http://localhost:8081/api/v1/passengers");
    }

    @BeforeEach
    void setUp() {
        rideRepository.deleteAll();
        jdbcTemplate.execute("TRUNCATE TABLE " + TABLE_NAME + " RESTART IDENTITY");
    }

    @Test
    void givenTwoRides_whenFindAllRides_thenReturnAllRides() throws Exception {
        //given
        saveRide(ride);
        saveRide(secondRide);

        //when-then
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
    void givenTwoRides_whenFindAllRidesWithFilterByPrice_thenReturnRidesFilteredByPrice() throws Exception {
        //given
        saveRide(ride);
        saveRide(secondRide);

        //when-then
        mockMvc.perform(get(URI)
                        .param("ridePrice", BigDecimal.valueOf(25).toString())
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].destinationAddress", is("Minsk,Dombrouskaya,30")))
                .andExpect(jsonPath("$.content[0].rideStatus", is("ACCEPTED")))
                .andExpect(jsonPath("$.content[0].ridePrice", is(BigDecimal.valueOf(25).doubleValue())));
    }

    @Test
    void whenSaveRide_thenReturnSavedRide() throws Exception {
        //when-then
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
    void givenRideAndUpdatedRide_whenUpdateRide_thenReturnUpdatedRide() throws Exception {
        //given
        saveRide(ride);
        Ride rideToUpdate = Ride.builder()
                .id(ride.getId())
                .driverId(ride.getDriverId())
                .passengerId(ride.getPassengerId())
                .departureAddress(ride.getDepartureAddress())
                .destinationAddress(ride.getDestinationAddress())
                .rideStatus(ride.getRideStatus())
                .ridePrice(ride.getRidePrice())
                .rideOrderTime(ride.getRideOrderTime())
                .build();
        rideToUpdate.setDepartureAddress("Minsk,Gusouskoga,9");
        rideToUpdate.setRidePrice(BigDecimal.valueOf(36));

        //when-then
        mockMvc.perform(put(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(rideToUpdate))
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.departureAddress", is("Minsk,Gusouskoga,9")))
                .andExpect(jsonPath("$.ridePrice", is(BigDecimal.valueOf(36).intValue())));
    }

    @Test
    void givenSavedRide_whenDeleteById_thenReturnStatus204() throws Exception {
        //given
        saveRide(ride);

        //when-then
        mockMvc.perform(delete(URI + "/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenSavedRide_whenGetRideById_thenReturnFoundRide() throws Exception {
        //given
        saveRide(secondRide);

        //when-then
        mockMvc.perform(get(URI + "/{id}", 1L)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.destinationAddress", is("Minsk,Dombrouskaya,30")))
                .andExpect(jsonPath("$.departureAddress", is("Minsk,Bricketa,8")))
                .andExpect(jsonPath("$.driverId", is(1)));
    }

    private void saveRide(Ride ride) throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(ride))
                        .accept("application/json"))
                .andExpect(status().isCreated());
    }
}
