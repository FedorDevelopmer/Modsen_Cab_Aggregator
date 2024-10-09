package com.modsen.software.driver.component_testing.driver_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.mapper.DriverMapper;
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

public class DriverUpdateTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to update driver with id = {long} changing name to {string} and email to {string}")
    public void driverUpdateRequest(long id, String name, String email) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + DriverTestLoader.DRIVERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestDriver();
        Driver driver = Driver.builder()
                .id(id)
                .name(name)
                .surname("Doe")
                .email(email)
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - DriverTestLoader.MONTH_DURATION))
                .phoneNumber("+111-111-111")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();

        result = mockMvc.perform(put(DriverTestLoader.URI)
                .content(objectMapper.writeValueAsString(driverMapper.driverToRequest(driver)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("updated driver with name {string} and email {string} should be returned")
    public void responseContainsSavedDriver(String name, String email) throws Exception {
        String driverString = result.getResponse().getContentAsString();
        Driver driver = objectMapper.readValue(driverString, Driver.class);
        Assertions.assertEquals(name, driver.getName());
        Assertions.assertEquals(email, driver.getEmail());
    }

    @When("I try to update driver with id = {long} changing email to {string}")
    public void driverUpdateRequestWithDuplicateEmail(Long id, String email) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + DriverTestLoader.DRIVERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestDriver();
        Driver driver = Driver.builder()
                .name("John")
                .surname("Doe")
                .email(email)
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - DriverTestLoader.MONTH_DURATION))
                .phoneNumber("+222-222-222")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();
        saveDriver(driver);
        Driver driverToUpdate = Driver.builder()
                .id(id)
                .name("John")
                .surname("Doe")
                .email(email)
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - DriverTestLoader.MONTH_DURATION))
                .phoneNumber("+222-111-222")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();

        result = mockMvc.perform(put(DriverTestLoader.URI)
                .content(objectMapper.writeValueAsString(driverMapper.driverToRequest(driverToUpdate)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("the response should indicate that updated email already owned by another driver with code {int}")
    public void responseIndicatesDuplicateEmail(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestDriver() throws Exception {
        Driver testDriver = Driver.builder()
                .name("Jack")
                .surname("Sparrow")
                .email("j.sp@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - DriverTestLoader.MONTH_DURATION))
                .phoneNumber("+243-424-98")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();
        saveDriver(testDriver);
    }

    private void saveDriver(Driver driver) throws Exception {
        DriverRequestTO driverRequest = driverMapper.driverToRequest(driver);
        mockMvc.perform(post(DriverTestLoader.URI)
                .content(objectMapper.writeValueAsString(driverRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
