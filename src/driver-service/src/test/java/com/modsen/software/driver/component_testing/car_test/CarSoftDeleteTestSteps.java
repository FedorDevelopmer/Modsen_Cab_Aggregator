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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CarSoftDeleteTestSteps {
    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to delete car with id = {long}")
    public void carDeleteRequest(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + CarTestLoader.CARS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestCar();
        result = mockMvc.perform(delete(CarTestLoader.URI + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("the response should indicate successful delete of car with code {int}\\(NO_CONTENT)")
    public void responseIndicatesSuccessfulDelete(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified for delete car not found")
    public void responseCodeIsNotFound(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestCar() throws Exception {
        Driver testDriver = Driver.builder()
                .name("Jack")
                .surname("Sparrow")
                .email("j.sp@gmail.com")
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
        Car testCar = Car.builder()
                .driverId(1L)
                .color(Color.GRAY)
                .brand("Honda")
                .registrationNumber("7 TAX 4332")
                .inspectionDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        saveCar(testCar);
    }

    private void saveCar(Car car) throws Exception {
        CarRequestTO carRequest = carMapper.carToRequest(car);
        mockMvc.perform(post(CarTestLoader.URI)
                .content(objectMapper.writeValueAsString(carRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
