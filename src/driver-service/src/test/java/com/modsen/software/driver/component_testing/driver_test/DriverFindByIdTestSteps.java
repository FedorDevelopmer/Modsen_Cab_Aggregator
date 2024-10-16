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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class DriverFindByIdTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request driver by id = {long} from database through service")
    public void requestDriverById(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + DriverTestLoader.DRIVERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestDriver();
        result = mockMvc.perform(get(DriverTestLoader.URI + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Then("find by id request complete with code {int} \\(OK) for driver")
    public void responseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("returned driver must not be null, with name {string}, email {string} and phone number {string}")
    public void responseContainsSavedDriver(String name, String email, String phone) throws Exception {
        String driverString = result.getResponse().getContentAsString();
        Driver driver = objectMapper.readValue(driverString, Driver.class);
        Assertions.assertEquals(name, driver.getName());
        Assertions.assertEquals(email, driver.getEmail());
        Assertions.assertEquals(phone, driver.getPhoneNumber());
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified driver not found")
    public void responseCodeIsNotFound(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestDriver() throws Exception {
        Driver testDriver = Driver.builder()
                .name("Jack")
                .surname("Sparrow")
                .email("j.sp@mail.com")
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
