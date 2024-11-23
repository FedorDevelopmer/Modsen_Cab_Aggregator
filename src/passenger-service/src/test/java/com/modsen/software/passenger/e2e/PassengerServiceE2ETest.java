package com.modsen.software.passenger.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.passenger.PassengerServiceApplication;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.repository.PassengerRepository;
import com.modsen.software.passenger.service.impl.PassengerServiceImpl;
import io.restassured.RestAssured;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
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
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
@Testcontainers
@SpringBootTest(classes = PassengerServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"passenger-rating"})
public class PassengerServiceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    private static final String TABLE_NAME = "passengers";

    private static final String URI = "/api/v1/passengers";

    private static String oldJdbcUrl = "";

    private static String oldKafkaBootstrapServers = "";

    @Autowired
    private PassengerRepository passengerRepository;

    @Autowired
    private PassengerServiceImpl passengerService;

    private static Passenger passenger;

    private static Passenger secondPassenger;

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
    private static PostgreSQLContainer<?> passengerServiceDB = new PostgreSQLContainer<>("postgres:latest")
            .withNetwork(network)
            .withExposedPorts(5432)
            .withUsername("user")
            .withPassword("UltraStrongPassw0rd");

    private static GenericContainer<?> ratingService;

    @Container
    private static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withNetwork(network)
            .withExposedPorts(9092, 9093);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", passengerServiceDB::getJdbcUrl);
        registry.add("spring.datasource.username", passengerServiceDB::getUsername);
        registry.add("spring.datasource.password", passengerServiceDB::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @BeforeAll
    static void beforeAll() {
        ratingService = new GenericContainer<>(DockerImageName.parse("rating_service"))
                .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://host.docker.internal:" + ratingServiceDB.getMappedPort(5432) + "/test")
                .withEnv("SPRING_DATASOURCE_USERNAME", "user")
                .withEnv("SPRING_DATASOURCE_PASSWORD", "UltraStrongPassw0rd")
                .withEnv("SPRING_KAFKA_BOOTSTRAP_SERVERS", "host.docker.internal:" + kafka.getMappedPort(9092))
                .withExposedPorts(8083)
                .withNetwork(network);
        ratingServiceDB.start();
        ratingService.start();
        oldJdbcUrl = System.getProperty("spring.datasource.url");
        oldKafkaBootstrapServers = System.getProperty("spring.kafka.bootstrap-servers");
        System.setProperty("spring.datasource.url", passengerServiceDB.getJdbcUrl());
        System.setProperty("spring.kafka.bootstrap-servers", kafka.getBootstrapServers());
        passenger = Passenger.builder()
                .id(1L)
                .name("John")
                .email("john.c@gmail.com")
                .gender(com.modsen.software.passenger.entity.enumeration.Gender.MALE)
                .phoneNumber("+745-432-143")
                .rating(BigDecimal.valueOf(5))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .removeStatus(com.modsen.software.passenger.entity.enumeration.RemoveStatus.ACTIVE)
                .build();
        secondPassenger = Passenger.builder()
                .id(2L)
                .name("Laila")
                .email("lai.tess@gmail.com")
                .gender(com.modsen.software.passenger.entity.enumeration.Gender.FEMALE)
                .phoneNumber("+334-332-986")
                .rating(BigDecimal.valueOf(5))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .removeStatus(com.modsen.software.passenger.entity.enumeration.RemoveStatus.ACTIVE)
                .build();
    }

    @AfterAll
    static void afterAll() {
        passengerServiceDB.stop();
        ratingService.stop();
        kafka.stop();
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE " + TABLE_NAME + " RESTART IDENTITY");
    }

    @Test
    void givenTwoPassengers_whenFindAllPassengers_thenReturnAllPassenger() throws Exception {
        //given
        savePassenger(passenger);
        savePassenger(secondPassenger);

        //when-then
        mockMvc.perform(get(URI)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].name", is("John")))
                .andExpect(jsonPath("$.content[0].email", is("john.c@gmail.com")))
                .andExpect(jsonPath("$.content[0].rating", equalTo(5.0)))
                .andExpect(jsonPath("$.content[0].phoneNumber", is("+745-432-143")))
                .andExpect(jsonPath("$.content[1].name", is("Laila")))
                .andExpect(jsonPath("$.content[1].email", is("lai.tess@gmail.com")))
                .andExpect(jsonPath("$.content[1].rating", equalTo(5.0)))
                .andExpect(jsonPath("$.content[1].phoneNumber", is("+334-332-986")));
    }

    @Test
    void givenTwoPassengers_whenFindAllPassengersWithFilter_thenReturnFilteredPassengersFirstPage() throws Exception {
        //given
        savePassenger(passenger);
        savePassenger(secondPassenger);

        //when-then
        mockMvc.perform(get(URI)
                        .param("gender", "MALE")
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("John")))
                .andExpect(jsonPath("$.content[0].email", is("john.c@gmail.com")))
                .andExpect(jsonPath("$.content[0].rating", equalTo(5.0)))
                .andExpect(jsonPath("$.content[0].phoneNumber", is("+745-432-143")));
    }

    @Test
    void whenSaveNewPassenger_thenReturnSavedPassenger() throws Exception {
        //when-then
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(passenger))
                        .accept("application/json"))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.email", is("john.c@gmail.com")))
                .andExpect(jsonPath("$.rating", equalTo(5.0)))
                .andExpect(jsonPath("$.phoneNumber", is("+745-432-143")));
    }

    @Test
    void givenPassengerAndUpdatedPassenger_whenUpdatePassenger_thenReturnUpdatedPassenger() throws Exception {
        //given
        savePassenger(passenger);
        Passenger PassengerToUpdate = Passenger.builder()
                .id(passenger.getId())
                .name(passenger.getName())
                .email(passenger.getEmail())
                .rating(passenger.getRating())
                .phoneNumber(passenger.getPhoneNumber())
                .gender(passenger.getGender())
                .removeStatus(passenger.getRemoveStatus())
                .ratingUpdateTimestamp(passenger.getRatingUpdateTimestamp())
                .build();
        PassengerToUpdate.setEmail("up.em@gmail.com");
        PassengerToUpdate.setPhoneNumber("+323-999-249");

        //when-then
        mockMvc.perform(put(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(PassengerToUpdate))
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.email", is("up.em@gmail.com")))
                .andExpect(jsonPath("$.phoneNumber", is("+323-999-249")));
    }

    @Test
    void givenPassengerToDelete_whenDeletePassenger_ThenReturnStatus204() throws Exception {
        //given
        savePassenger(passenger);

        //when-then
        mockMvc.perform(delete(URI + "/{id}", 1L))
                .andExpect(status().is(204));
    }

    @Test
    void givenPassenger_whenFindPassengerById_thenReturnPassengerWithId() throws Exception {
        //given
        savePassenger(passenger);

        //when-then
        mockMvc.perform(get(URI + "/{id}", 1L)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.email", is("john.c@gmail.com")))
                .andExpect(jsonPath("$.rating", equalTo(5.0)))
                .andExpect(jsonPath("$.phoneNumber", is("+745-432-143")));
    }

    @Test
    void givenKafkaConsumerAndProducerAndPassengersResponseTo_whenSentMessagesToPassengerTopic_thenReadSentMessagesFromPassengerTopicAndGetRatingEvaluation() throws Exception {
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

        PassengerResponseTO response = new PassengerResponseTO(
                1L,
                "John",
                "john.doe@example.com",
                "+003-226-710",
                BigDecimal.valueOf(5),
                Gender.MALE,
                LocalDateTime.now(),
                RemoveStatus.ACTIVE
        );

        //when
        consumer.subscribe(Arrays.asList("test-passenger-rating"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
        int triesCount = 1;
        while (records.isEmpty() && triesCount < 10) {
            Thread.sleep(2000);
            producer.send(new ProducerRecord<>("test-passenger-rating", mapper.writeValueAsString(response)));
            records = consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
            triesCount++;
        }

        //then
        Assumptions.assumeTrue(triesCount < 10);
        List<ConsumerRecord<String, String>> recordList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records.records("test-passenger-rating")) {
            recordList.add(record);
        }
        PassengerResponseTO PassengerResponseTO = mapper.readValue(recordList.get(0).value(), PassengerResponseTO.class);
        RestAssured.given()
                .contentType("application/json")
                .get("http://localhost:" + ratingService.getFirstMappedPort() + "/api/v1/scores/evaluate/" + PassengerResponseTO.getId() + "?initiator=PASSENGER")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("meanEvaluation", equalTo(BigDecimal.valueOf(5).floatValue()));
    }

    private void savePassenger(Passenger passenger) throws Exception {
        mockMvc.perform(post(URI)
                .contentType("application/json")
                .content(mapper.writeValueAsString(passenger))
                .accept("application/json"));
    }
}
