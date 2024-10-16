package com.modsen.software.driver.component_testing.driver_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.mapper.DriverMapper;
import io.cucumber.java.en.And;
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
import java.util.LinkedHashMap;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class DriverFindAllTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DriverMapper driverMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request all drivers from database through service")
    public void requestAllDrivers() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + DriverTestLoader.DRIVERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestDrivers();
        result = mockMvc.perform(get(DriverTestLoader.URI).contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("all drivers request complete with code {int} \\(OK)")
    public void allDriversCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("all male drivers request complete with code {int} \\(OK)")
    public void allMalesResponseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @And("first drivers page should be returned")
    public void responseContainDriversPage() throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer driversCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(driversCount > 0);
    }

    @When("I request all male drivers from database through service")
    public void requestAllMaleDrivers() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + DriverTestLoader.DRIVERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestDrivers();
        result = mockMvc.perform(get(DriverTestLoader.URI)
                .param("gender", Gender.MALE.name())
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @And("first page of filtered with gender {string} drivers should be returned")
    public void responseContainMaleDriversPage(String gender) throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer driversCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        List<LinkedHashMap<String, Object>> driversMaps = JsonPath.parse(contentString).read("$.content", List.class);
        for (LinkedHashMap<String, Object> driverMap : driversMaps) {
            Assertions.assertEquals(gender, driverMap.get("gender"));
        }
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(driversCount > 0);
    }

    private void saveTestDrivers() throws Exception {
        Driver testDriver = Driver.builder()
                .name("John")
                .surname("Conor")
                .email("john.c@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - DriverTestLoader.MONTH_DURATION))
                .phoneNumber("+323-322-243")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();

        Driver testDriverSecond = Driver.builder()
                .name("Mattew")
                .surname("Dante")
                .email("mt.dt@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - DriverTestLoader.MONTH_DURATION))
                .phoneNumber("+323-322-111")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();
        saveDriver(testDriver);
        saveDriver(testDriverSecond);
    }

    private void saveDriver(Driver driver) throws Exception {
        DriverRequestTO driverRequest = driverMapper.driverToRequest(driver);
        mockMvc.perform(post(DriverTestLoader.URI)
                .content(objectMapper.writeValueAsString(driverRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
