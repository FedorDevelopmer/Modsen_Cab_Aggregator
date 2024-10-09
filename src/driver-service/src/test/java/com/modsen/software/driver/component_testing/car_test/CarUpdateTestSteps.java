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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class CarUpdateTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to update car with id = {long} changing brand to {string} and registration number to {string}")
    public void driverUpdateRequest(Long id, String brand, String registrationNumber) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + CarTestLoader.CARS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestCar();
        Car testCar = Car.builder()
                .driverId(1L)
                .color(Color.RED)
                .brand("Lexus")
                .registrationNumber("7 TAX 1432")
                .inspectionDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        saveCar(testCar);
        Car carToUpdate = Car.builder()
                .id(id)
                .driverId(1L)
                .color(Color.GRAY)
                .brand(brand)
                .registrationNumber(registrationNumber)
                .inspectionDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        result = mockMvc.perform(put(CarTestLoader.URI)
                .content(objectMapper.writeValueAsString(carMapper.carToRequest(carToUpdate)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("updated driver with brand {string} and registration number to {string} should be returned")
    public void responseContainsSavedCar(String brand, String registrationNumber) throws Exception {
        String carString = result.getResponse().getContentAsString();
        Car car = objectMapper.readValue(carString, Car.class);
        Assertions.assertEquals(brand, car.getBrand());
        Assertions.assertEquals(registrationNumber, car.getRegistrationNumber());
    }

    @When("I try to update car with id = {long} changing registration number to {string}")
    public void carUpdateRequestWithDuplicateRegistrationNumber(Long id, String registrationNumber) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + CarTestLoader.CARS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestCar();
        Car carToUpdate = Car.builder()
                .id(id)
                .driverId(1L)
                .color(Color.GRAY)
                .brand("Honda")
                .registrationNumber(registrationNumber)
                .inspectionDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        result = mockMvc.perform(put(CarTestLoader.URI)
                .content(objectMapper.writeValueAsString(carMapper.carToRequest(carToUpdate)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("the response should indicate that updated registration number already owned by another driver with code {int}")
    public void responseIndicatesDuplicateEmail(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestCar() throws Exception {
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
                .brand("Honda")
                .registrationNumber("7 TAX 4543")
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
