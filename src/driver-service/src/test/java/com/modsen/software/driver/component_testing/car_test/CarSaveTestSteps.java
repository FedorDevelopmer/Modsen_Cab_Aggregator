package com.modsen.software.driver.component_testing.car_test;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CarSaveTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @When("I save new car with brand {string} and registration number {string} for driver with id {long}")
    public void saveNewCar(String brand, String registrationNumber, Long driverId) throws Exception {
        saveTestDriver();
        Car testCar = Car.builder()
                .driverId(driverId)
                .color(Color.GRAY)
                .brand(brand)
                .registrationNumber(registrationNumber)
                .inspectionDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();

        result = mockMvc.perform(post(CarTestLoader.URI)
                .content(objectMapper.writeValueAsString(carMapper.carToRequest(testCar)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("saved car with brand {string}, registration number {string} and driver id {long} should be returned")
    public void responseContainsSavedCar(String brand, String registrationNumber, Long driverId) throws Exception {
        String carString = result.getResponse().getContentAsString();
        Car car = objectMapper.readValue(carString, Car.class);
        Assertions.assertEquals(brand, car.getBrand());
        Assertions.assertEquals(registrationNumber, car.getRegistrationNumber());
        Assertions.assertEquals(driverId, car.getDriverId());
    }

    @Then("the response should indicate that registration number already owned by another car with code {int}")
    public void responseIndicatesDuplicateRegistrationNumber(Integer code) {
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
        mockMvc.perform(post("/api/v1/drivers")
                .content(objectMapper.writeValueAsString(testDriver))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
