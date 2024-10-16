package com.modsen.software;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.modsen.software.driver.DriverServiceApplication;
import com.modsen.software.driver.dto.RatingEvaluationResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.impl.DriverServiceImpl;
import jakarta.transaction.Transactional;
import static org.hamcrest.Matchers.*;
import org.junit.jupiter.api.*;
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
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = DriverServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DriverIntegrationTest {

    @LocalServerPort
    private Integer port;

    private static final String TABLE_NAME = "drivers";

    private static final String URI = "/api/v1/drivers";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    private static final long MONTH_DURATION = 86_400_000 * 30L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static WireMockServer ratingServiceMock = new WireMockServer(options().port(8083));

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private DriverServiceImpl driverService;

    private static Driver driver;

    private static Driver secondDriver;

    private static Car car;

    private static Car secondCar;

    @Autowired
    private ObjectMapper mapper;

    @BeforeAll
    static void beforeAll() throws Exception {
        postgres.start();
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
                .cars(Set.of(car,secondCar))
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
        driverRepository.deleteAll();
        jdbcTemplate.execute("TRUNCATE TABLE " + TABLE_NAME + " RESTART IDENTITY CASCADE");
    }

    @Test
    void testGetAllDrivers() throws Exception {
        saveDriver(driver);
        saveDriver(secondDriver);
        mockMvc.perform(get(URI)
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
    void testGetAllDriversWithFilter() throws Exception {
        saveDriver(driver);
        saveDriver(secondDriver);
        mockMvc.perform(get(URI)
                        .param("gender",Gender.MALE.name())
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
    void testSaveDriver() throws Exception {
        mockMvc.perform(post(URI)
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
    void testUpdateDriver() throws Exception {
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
        mockMvc.perform(put(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(driverToUpdate))
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.email", is("up.em@gmail.com")))
                .andExpect(jsonPath("$.phoneNumber", is("+323-999-249")));
    }

    @Test
    void testDeleteDriver() throws Exception {
        saveDriver(driver);
        mockMvc.perform(delete(URI + "/{id}", 1L))
                .andExpect(status().is(204));
    }

    @Test
    void testFindDriverById() throws Exception {
        saveDriver(driver);
        mockMvc.perform(get(URI + "/{id}", 1L)
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

    private void saveDriver(Driver driver) throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(driver))
                        .accept("application/json"))
                .andExpect(status().is(201));
    }
}
