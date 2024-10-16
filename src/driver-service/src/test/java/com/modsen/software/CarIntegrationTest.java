package com.modsen.software;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.DriverServiceApplication;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.impl.CarServiceImpl;
import com.modsen.software.driver.shedule.DriverServiceSchedule;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@AutoConfigureMockMvc
@SpringBootTest(classes = DriverServiceApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarIntegrationTest {

    @LocalServerPort
    private Integer port;

    private static final String URI = "/api/v1/cars";

    private static final String CARS_TABLE_NAME = "cars";

    private static final String DRIVERS_TABLE_NAME = "drivers";

    private static final long MONTH_DURATION = 86_400_000 * 30L;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DriverRepository driverRepository;

    @MockBean
    private DriverServiceSchedule driverServiceSchedule;

    @Autowired
    private CarServiceImpl carService;

    private static Car car;

    private static Car secondCar;

    private static Driver driver;

    @Autowired
    private ObjectMapper mapper;

    @BeforeAll
    static void beforeAll() throws Exception {
        postgres.start();

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

    }

    @AfterAll
    static void afterAll() {
        postgres.stop();
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
    void setUp() throws Exception{
        driverRepository.deleteAll();
        carRepository.deleteAll();
        jdbcTemplate.execute("TRUNCATE TABLE " + CARS_TABLE_NAME + " RESTART IDENTITY");
        jdbcTemplate.execute("TRUNCATE TABLE " + DRIVERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveDriver(driver);
    }

    @Test
    void testGetAllCarScores() throws Exception {
        saveCar(car);
        saveCar(secondCar);
        mockMvc.perform(get(URI)
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
    void testGetAllCarScoresWithFilter() throws Exception {
        saveCar(car);
        saveCar(secondCar);
        mockMvc.perform(get(URI)
                        .param("brand","Honda")
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
    void testSaveCarScore() throws Exception {
        mockMvc.perform(post(URI)
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
    void testUpdateCarScore() throws Exception {
        saveCar(car);
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
        mockMvc.perform(put(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(carToUpdate))
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.registrationNumber", is("7TAX1111")))
                .andExpect(jsonPath("$.inspectionDurationMonth", is(24)));
    }

    @Test
    void testDeleteCarScore() throws Exception {
        saveCar(car);
        mockMvc.perform(delete( URI + "/{id}", 1L))
                .andExpect(status().is(204));
    }

    @Test
    void testFindCarScoreById() throws Exception {
        saveCar(car);
        mockMvc.perform(get(URI + "/{id}", 1L)
                        .contentType("application/json")
                        .accept("application/json"))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.driverId", is(1)))
                .andExpect(jsonPath("$.brand", is("Lexus")))
                .andExpect(jsonPath("$.color", is("GRAY")))
                .andExpect(jsonPath("$.registrationNumber", is("7TAX4584")));
    }

    private void saveCar(Car car) throws Exception {
        mockMvc.perform(post(URI)
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(car))
                        .accept("application/json"))
                .andExpect(status().is(201));
    }

    private void saveDriver(Driver driver) throws Exception {
        mockMvc.perform(post("/api/v1/drivers")
                        .contentType("application/json")
                        .content(mapper.writeValueAsString(driver))
                        .accept("application/json"))
                .andExpect(status().is(201));
    }
}
