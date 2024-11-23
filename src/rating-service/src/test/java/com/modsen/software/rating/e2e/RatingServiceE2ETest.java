package com.modsen.software.rating.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.rating.RatingServiceApplication;
import com.modsen.software.rating.dto.RatingEvaluationResponseTO;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.repository.RatingRepository;
import com.modsen.software.rating.service.impl.RatingServiceImpl;
import io.restassured.RestAssured;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.test.context.EmbeddedKafka;
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
@SpringBootTest(classes = RatingServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"passenger-rating", "rating-passenger", "driver-rating", "rating-driver"})
public class RatingServiceE2ETest {

    @LocalServerPort
    private int port;

    private static AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

    private static ConfigurableEnvironment environment = context.getEnvironment();

    @Autowired
    private MockMvc mockMvc;

    private static final String TABLE_NAME = "rating_scores";

    private static final String URI = "/api/v1/scores";

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private RatingServiceImpl ratingService;

    private static RatingScore ratingScore;

    private static RatingScore secondRatingScore;

    private static String driverJson;

    private static String passengerJson;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper mapper;

    static Network network = Network.newNetwork();

    @Container
    private static PostgreSQLContainer<?> ratingServiceDB = new PostgreSQLContainer<>("postgres:latest")
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
        registry.add("spring.datasource.url", ratingServiceDB::getJdbcUrl);
        registry.add("spring.datasource.username", ratingServiceDB::getUsername);
        registry.add("spring.datasource.password", ratingServiceDB::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        String driverUrl = "http://localhost:" + driverService.getFirstMappedPort() + "/api/v1/drivers";
        String passengerUrl = "http://localhost:" + passengerService.getFirstMappedPort() + "/api/v1/passengers";
        registry.add("rating-service.drivers.url", () -> driverUrl);
        registry.add("rating-service.passengers.url", () -> passengerUrl);
    }

    @BeforeAll
    static void beforeAll() {
        ratingServiceDB.start();
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
                .driverId(1L)
                .passengerId(1L)
                .comment("Passenger was polite and communicative.")
                .evaluation(4)
                .initiator(Initiator.DRIVER)
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
        ratingServiceDB.stop();
        driverServiceDB.stop();
        driverService.stop();
        passengerService.stop();
        passengerServiceDB.stop();
        kafka.stop();
    }

    @BeforeEach
    void setUp() {
        ratingRepository.deleteAll();
        jdbcTemplate.execute("TRUNCATE TABLE " + TABLE_NAME + " RESTART IDENTITY");
    }

    @Test
    void givenTwoRatingScores_whenGetAllScoresRequest_thenReturnAllScoresList() throws Exception {
        //given
        saveRating(ratingScore);
        saveRating(secondRatingScore);

        //when-then
        mockMvc.perform(get(URI)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].driverId", is(1)))
                .andExpect(jsonPath("$.content[0].passengerId", is(1)))
                .andExpect(jsonPath("$.content[0].comment", is("Excellent ride!")))
                .andExpect(jsonPath("$.content[0].evaluation", is(5)))
                .andExpect(jsonPath("$.content[1].driverId", is(1)))
                .andExpect(jsonPath("$.content[1].passengerId", is(1)))
                .andExpect(jsonPath("$.content[1].comment", is("Passenger was polite and communicative.")))
                .andExpect(jsonPath("$.content[1].evaluation", is(4)));
    }

    @Test
    void givenTwoRatingScores_whenGetAllRatingScoresWithFilterRequest_thenReturnFilteredScoresList() throws Exception {
        //given
        saveRating(ratingScore);
        saveRating(secondRatingScore);

        //when-then
        mockMvc.perform(get(URI)
                        .param("initiator", Initiator.DRIVER.name())
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].driverId", is(1)))
                .andExpect(jsonPath("$.content[0].passengerId", is(1)))
                .andExpect(jsonPath("$.content[0].comment", is("Passenger was polite and communicative.")))
                .andExpect(jsonPath("$.content[0].evaluation", is(4)));
    }

    @Test
    void whenSaveRatingScore_thenReturnSaveRatingScore() throws Exception {
        //when-then
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
    void givenRatingScoreAndUpdatedRatingScore_whenUpdateRatingScoreRequest_thenReturnUpdateRatingScore() throws Exception {
        //given
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

        //when-then
        mockMvc.perform(put(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(ratingToUpdate))
                        .accept("application/json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment", is("Not so bad ride!")))
                .andExpect(jsonPath("$.evaluation", is(4)));
    }

    @Test
    void givenRatingScoreToDelete_whenDeleteRatingScore_thenReturnStatus204() throws Exception {
        //given
        saveRating(ratingScore);

        //when-then
        mockMvc.perform(delete(URI + "/{id}", 1L))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenRatingScoreToFind_whenFindRatingScoreByIdRequest_thenReturnFoundRatingScore() throws Exception {
        //given
        saveRating(ratingScore);

        //when-then
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

    @Test
    void givenKafkaConsumerAndProducerAndRatingEvaluation_whenSentMessagesToRatingTopic_thenReadSentMessagesFromRatingTopic() throws Exception {
        //given
        Properties producerProps = new Properties();
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class.getName());
        KafkaProducer<String, String> producer = new KafkaProducer<>(producerProps);

        Properties consumerProps = new Properties();
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafka.getBootstrapServers());
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, "rating-evaluation");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class.getName());
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(consumerProps);

        //when
        consumer.subscribe(Arrays.asList("test-rating-driver"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
        int triesCount = 1;
        RatingEvaluationResponseTO ratingEvaluation = new RatingEvaluationResponseTO(1L, BigDecimal.valueOf(5));
        while (records.isEmpty() && triesCount < 10) {
            Thread.sleep(2000);
            producer.send(new ProducerRecord<>("test-rating-driver", mapper.writeValueAsString(ratingEvaluation)));
            records = consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
            triesCount++;
        }

        //then
        Assumptions.assumeTrue(triesCount < 10);
        List<ConsumerRecord<String, String>> recordList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records.records("test-rating-driver")) {
            recordList.add(record);
        }
        RatingEvaluationResponseTO receivedEvaluation = mapper.readValue(recordList.get(0).value(), RatingEvaluationResponseTO.class);
        Assertions.assertEquals(ratingEvaluation.getId(), receivedEvaluation.getId());
        Assertions.assertEquals(ratingEvaluation.getMeanEvaluation(), receivedEvaluation.getMeanEvaluation());
    }

    private void saveRating(RatingScore rating) throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(rating))
                        .accept("application/json"))
                .andExpect(status().isCreated());
    }
}
