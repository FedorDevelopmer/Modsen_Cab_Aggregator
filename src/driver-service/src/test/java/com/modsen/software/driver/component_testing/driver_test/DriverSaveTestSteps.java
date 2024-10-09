package com.modsen.software.driver.component_testing.driver_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.mapper.DriverMapper;
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

public class DriverSaveTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @When("I save new driver with name {string}, email {string} and phone number {string}")
    public void saveNewDriver(String name, String email, String phone) throws Exception {
        Driver driver = Driver.builder()
                .name(name)
                .surname("Doe")
                .email(email)
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - DriverTestLoader.MONTH_DURATION))
                .phoneNumber(phone)
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();

        result = mockMvc.perform(post(DriverTestLoader.URI)
                .content(objectMapper.writeValueAsString(driverMapper.driverToRequest(driver)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("saved driver with name {string}, email {string}, phone number {string} and defined id should be returned")
    public void responseContainsSavedDriver(String name, String email, String phone) throws Exception {
        String driverString = result.getResponse().getContentAsString();
        Driver driver = objectMapper.readValue(driverString, Driver.class);
        Assertions.assertEquals(name, driver.getName());
        Assertions.assertEquals(email, driver.getEmail());
        Assertions.assertEquals(phone, driver.getPhoneNumber());
    }

    @Then("the response should indicate that email already owned by another driver with code {int}")
    public void responseIndicatesDuplicateEmail(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }
}
