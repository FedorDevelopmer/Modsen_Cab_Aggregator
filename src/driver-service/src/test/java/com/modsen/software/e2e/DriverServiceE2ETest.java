package com.modsen.software.e2e;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.DriverServiceApplication;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import io.restassured.RestAssured;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
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
@SpringBootTest(classes = DriverServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1, topics = {"driver-rating"})
public class DriverServiceE2ETest {

    @LocalServerPort
    private int port;

    @Autowired
    private MockMvc mockMvc;

    private static final String CARS_TABLE_NAME = "cars";

    private static final String DRIVERS_TABLE_NAME = "drivers";

    private static final String DRIVERS_URI = "/api/v1/drivers";

    private static final String CARS_URI = "/api/v1/cars";

    private static final long MONTH_DURATION = 86_400_000 * 30L;

    private static Driver driver;

    private static Driver secondDriver;

    private static Car car;

    private static Car secondCar;

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

    private static GenericContainer<?> ratingService;

    @Container
    private static KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:latest"))
            .withNetwork(network)
            .withExposedPorts(9092, 9093);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", driverServiceDB::getJdbcUrl);
        registry.add("spring.datasource.username", driverServiceDB::getUsername);
        registry.add("spring.datasource.password", driverServiceDB::getPassword);
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
        driverServiceDB.start();
        ratingService.start();
        car = Car.builder()
                .id(1L)
                .driverId(1L)
                .color(Color.GRAY)
                .brand("Lexus")
                .registrationNumber("7TAX4584")
                .inspectionDate(new Date(System.currentTimeMillis() - MONTH_DURATION))
                .inspectionDurationMonth(12)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        secondCar = Car.builder()
                .id(2L)
                .driverId(1L)
                .color(Color.RED)
                .brand("Honda")
                .registrationNumber("7TAX4243")
                .inspectionDate(new Date(System.currentTimeMillis() - MONTH_DURATION))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        driver = Driver.builder()
                .id(1L)
                .name("John")
                .surname("Conor")
                .email("john.c@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - MONTH_DURATION))
                .phoneNumber("+323-322-243")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();
        secondDriver = Driver.builder()
                .id(2L)
                .name("Alex")
                .surname("Kolin")
                .email("kol.a@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - MONTH_DURATION))
                .phoneNumber("+124-435-322")
                .gender(Gender.FEMALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();
    }

    @AfterAll
    static void afterAll() {
        ratingServiceDB.stop();
        driverServiceDB.stop();
        kafka.stop();
    }

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE \"" + DRIVERS_TABLE_NAME + "\" RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE \"" + CARS_TABLE_NAME + "\" RESTART IDENTITY");
    }

    @Test
    void givenTwoDrivers_whenFindAllDrivers_thenReturnAllDriver() throws Exception {
        //given
        saveDriver(driver);
        saveDriver(secondDriver);

        //when-then
        mockMvc.perform(get(DRIVERS_URI)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].name", is("John")))
                .andExpect(jsonPath("$.content[0].surname", is("Conor")))
                .andExpect(jsonPath("$.content[0].email", is("john.c@gmail.com")))
                .andExpect(jsonPath("$.content[0].rating", equalTo(5.0)))
                .andExpect(jsonPath("$.content[0].phoneNumber", is("+323-322-243")))
                .andExpect(jsonPath("$.content[1].name", is("Alex")))
                .andExpect(jsonPath("$.content[1].surname", is("Kolin")))
                .andExpect(jsonPath("$.content[1].email", is("kol.a@gmail.com")))
                .andExpect(jsonPath("$.content[1].rating", equalTo(5.0)))
                .andExpect(jsonPath("$.content[1].phoneNumber", is("+124-435-322")));
    }

    @Test
    void givenTwoDrivers_whenFindAllDriversWithFilter_thenReturnFilteredDriversFirstPage() throws Exception {
        //given
        saveDriver(driver);
        saveDriver(secondDriver);

        //when-then
        mockMvc.perform(get(DRIVERS_URI)
                        .param("gender", "MALE")
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].name", is("John")))
                .andExpect(jsonPath("$.content[0].surname", is("Conor")))
                .andExpect(jsonPath("$.content[0].email", is("john.c@gmail.com")))
                .andExpect(jsonPath("$.content[0].rating", equalTo(5.0)))
                .andExpect(jsonPath("$.content[0].phoneNumber", is("+323-322-243")));
    }

    @Test
    void whenSaveNewDriver_thenReturnSavedDriver() throws Exception {
        //when-then
        mockMvc.perform(post(DRIVERS_URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(driver))
                        .accept("application/json"))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.surname", is("Conor")))
                .andExpect(jsonPath("$.email", is("john.c@gmail.com")))
                .andExpect(jsonPath("$.rating", equalTo(5.0)))
                .andExpect(jsonPath("$.phoneNumber", is("+323-322-243")));
    }

    @Test
    void givenDriverAndUpdatedDriver_whenUpdateDriver_thenReturnUpdatedDriver() throws Exception {
        //given
        saveDriver(driver);
        Driver driverToUpdate = Driver.builder()
                .id(driver.getId())
                .name(driver.getName())
                .surname(driver.getSurname())
                .email(driver.getEmail())
                .rating(driver.getRating())
                .birthDate(driver.getBirthDate())
                .phoneNumber(driver.getPhoneNumber())
                .gender(driver.getGender())
                .removeStatus(driver.getRemoveStatus())
                .ratingUpdateTimestamp(driver.getRatingUpdateTimestamp())
                .cars(driver.getCars())
                .build();
        driverToUpdate.setEmail("up.em@gmail.com");
        driverToUpdate.setPhoneNumber("+323-999-249");

        //when-then
        mockMvc.perform(put(DRIVERS_URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(driverToUpdate))
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.email", is("up.em@gmail.com")))
                .andExpect(jsonPath("$.phoneNumber", is("+323-999-249")));
    }

    @Test
    void givenDriverToDelete_whenDeleteDriver_ThenReturnStatus204() throws Exception {
        //given
        saveDriver(driver);

        //when-then
        mockMvc.perform(delete(DRIVERS_URI + "/{id}", 1L))
                .andExpect(status().is(204));
    }

    @Test
    void givenDriver_whenFindDriverById_thenReturnDriverWithId() throws Exception {
        //given
        saveDriver(driver);

        //when-then
        mockMvc.perform(get(DRIVERS_URI + "/{id}", 1L)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.surname", is("Conor")))
                .andExpect(jsonPath("$.email", is("john.c@gmail.com")))
                .andExpect(jsonPath("$.rating", equalTo(5.0)))
                .andExpect(jsonPath("$.phoneNumber", is("+323-322-243")));
    }

    @Test
    void givenCarsWithDriver_whenFindAllCars_thenAllCars() throws Exception {
        //given
        saveDriver(driver);
        saveCar(car);
        saveCar(secondCar);

        //when-then
        mockMvc.perform(get(CARS_URI)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.content[0].driverId", is(1)))
                .andExpect(jsonPath("$.content[0].brand", is("Lexus")))
                .andExpect(jsonPath("$.content[0].color", is("GRAY")))
                .andExpect(jsonPath("$.content[0].registrationNumber", is("7TAX4584")))
                .andExpect(jsonPath("$.content[1].driverId", is(1)))
                .andExpect(jsonPath("$.content[1].brand", is("Honda")))
                .andExpect(jsonPath("$.content[1].color", is("RED")))
                .andExpect(jsonPath("$.content[1].registrationNumber", is("7TAX4243")));

    }

    @Test
    void givenCarsWithDriver_whenFindAllCarsWithFilter_thenReturnFilteredCars() throws Exception {
        //given
        saveDriver(driver);
        saveCar(car);
        saveCar(secondCar);

        //when-then
        mockMvc.perform(get(CARS_URI)
                        .param("brand", "Honda")
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.totalElements", is(1)))
                .andExpect(jsonPath("$.content[0].driverId", is(1)))
                .andExpect(jsonPath("$.content[0].brand", is("Honda")))
                .andExpect(jsonPath("$.content[0].color", is("RED")))
                .andExpect(jsonPath("$.content[0].registrationNumber", is("7TAX4243")));
    }

    @Test
    void givenDriver_whenSaveDriverCar_thenReturnSavedCar() throws Exception {
        //given
        saveDriver(driver);

        //when-then
        mockMvc.perform(post(CARS_URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(car))
                        .accept("application/json"))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.driverId", is(1)))
                .andExpect(jsonPath("$.brand", is("Lexus")))
                .andExpect(jsonPath("$.color", is("GRAY")))
                .andExpect(jsonPath("$.registrationNumber", is("7TAX4584")));
    }

    @Test
    void givenDriverCarAndUpdatedCar_whenUpdateCar_thenReturnUpdatedCar() throws Exception {
        //given
        saveDriver(driver);
        saveCar(car);

        //when-then
        Car carToUpdate = Car.builder()
                .id(car.getId())
                .driverId(car.getDriverId())
                .color(car.getColor())
                .brand(car.getBrand())
                .registrationNumber(car.getRegistrationNumber())
                .inspectionDate(car.getInspectionDate())
                .inspectionDurationMonth(car.getInspectionDurationMonth())
                .removeStatus(car.getRemoveStatus())
                .driver(car.getDriver())
                .build();

        carToUpdate.setRegistrationNumber("7TAX1111");
        carToUpdate.setInspectionDurationMonth(24);
        mockMvc.perform(put(CARS_URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(carToUpdate))
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.registrationNumber", is("7TAX1111")))
                .andExpect(jsonPath("$.inspectionDurationMonth", is(24)));
    }

    @Test
    void givenDriverWithCar_whenDeleteCar_thenReturnStatus204() throws Exception {
        //given
        saveDriver(driver);
        saveCar(car);

        //when-then
        mockMvc.perform(delete(CARS_URI + "/{id}", 1L))
                .andExpect(status().is(204));
    }

    @Test
    void givenDriverWithCar_whenFindCarById_thenReturnCarWithProvidedId() throws Exception {
        //given
        saveDriver(driver);
        saveCar(car);

        //when-then
        mockMvc.perform(get(CARS_URI + "/{id}", 1L)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.driverId", is(1)))
                .andExpect(jsonPath("$.brand", is("Lexus")))
                .andExpect(jsonPath("$.color", is("GRAY")))
                .andExpect(jsonPath("$.registrationNumber", is("7TAX4584")));
    }

    @Test
    void givenKafkaConsumerAndProducerAndDriverResponseTo_whenSentMessagesToDriverTopic_thenReadSentMessagesFromDriverTopic() throws Exception {
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

        DriverResponseTO response = new DriverResponseTO(1L,
                "John",
                "Doe",
                "john.doe@example.com",
                "+003-226-710",
                BigDecimal.valueOf(4.5),
                Gender.MALE,
                new Date(System.currentTimeMillis()),
                LocalDateTime.now(),
                RemoveStatus.ACTIVE,
                new HashSet<>());

        //when
        consumer.subscribe(Arrays.asList("test-rating-driver", "test-driver-rating"));
        ConsumerRecords<String, String> records = consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
        int triesCount = 1;
        while (records.isEmpty() && triesCount < 10) {
            Thread.sleep(2000);
            producer.send(new ProducerRecord<>("test-driver-rating", mapper.writeValueAsString(response)));
            records = consumer.poll(Duration.of(1, ChronoUnit.SECONDS));
            triesCount++;
        }
        Assumptions.assumeTrue(triesCount < 10);
        List<ConsumerRecord<String, String>> recordList = new ArrayList<>();
        for (ConsumerRecord<String, String> record : records.records("test-driver-rating")) {
            recordList.add(record);
        }

        //then
        DriverResponseTO driverResponseTO = mapper.readValue(recordList.get(0).value(), DriverResponseTO.class);
        RestAssured.given()
                .contentType("application/json")
                .get("http://localhost:" + ratingService.getFirstMappedPort() + "/api/v1/scores/evaluate/" + driverResponseTO.getId() + "?initiator=DRIVER")
                .then()
                .statusCode(200)
                .body("id", equalTo(1))
                .body("meanEvaluation", equalTo(BigDecimal.valueOf(5).floatValue()));
    }

    private void saveCar(Car car) throws Exception {
        mockMvc.perform(post(CARS_URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(car))
                        .accept("application/json"))
                .andExpect(status().is(201));
    }

    private void saveDriver(Driver driver) throws Exception {
        mockMvc.perform(post(DRIVERS_URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(driver))
                        .accept("application/json"))
                .andExpect(status().is(201));
    }
}
