package com.modsen.software.driver.component_testing.car_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.mapper.CarMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CarFindByIdTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request car by id = {long} from database through service")
    public void requestDriverById(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + CarTestLoader.CARS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestDriver();
        result = mockMvc.perform(get(CarTestLoader.URI + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Then("find by id request complete with code {int} \\(OK) for car")
    public void responseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("returned car must not be null, with brand {string} and registration number {string}")
    public void responseContainsSavedDriver(String brand, String registrationNumber) throws Exception {
        String carString = result.getResponse().getContentAsString();
        Car car = objectMapper.readValue(carString, Car.class);
        Assertions.assertEquals(brand, car.getBrand());
        Assertions.assertEquals(registrationNumber, car.getRegistrationNumber());
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified car not found")
    public void responseCodeIsNotFound(int code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestDriver() throws Exception {
        Driver testDriver = Driver.builder()
                .name("Jack")
                .surname("Sparrow")
                .email("j.sp@mail.com")
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .phoneNumber("+243-424-98")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();
        Car testCar = Car.builder()
                .driverId(1L)
                .color(Color.GRAY)
                .brand("Renault")
                .registrationNumber("7 TAX 5469")
                .inspectionDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        mockMvc.perform(post("/api/v1/drivers")
                .content(objectMapper.writeValueAsString(testDriver))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
        saveCar(testCar);
    }

    private void saveCar(Car car) throws Exception {
        CarRequestTO carRequest = carMapper.carToRequest(car);
        mockMvc.perform(post(CarTestLoader.URI)
                .content(objectMapper.writeValueAsString(carRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
