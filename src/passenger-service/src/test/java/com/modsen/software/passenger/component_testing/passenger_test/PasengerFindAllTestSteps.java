package com.modsen.software.passenger.component_testing.passenger_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.mapper.PassengerMapper;
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
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PasengerFindAllTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PassengerMapper passengerMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request all passengers from database through service")
    public void requestAllPassengers() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + PassengerTestLoader.PASSENGERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestPassengers();
        result = mockMvc.perform(get(PassengerTestLoader.URI).contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("all passengers request complete with code {int} \\(OK)")
    public void allPassengersCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("all male passengers request complete with code {int} \\(OK)")
    public void allMalesResponseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @And("first passengers page should be returned")
    public void responseContainPassengersPage() throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer passengersCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(passengersCount > 0);
    }

    @When("I request all male passengers from database through service")
    public void requestAllMalePassengers() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + PassengerTestLoader.PASSENGERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestPassengers();
        result = mockMvc.perform(get(PassengerTestLoader.URI)
                .param("gender", Gender.MALE.name())
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @And("first page of filtered with gender {string} passengers should be returned")
    public void responseContainMalePassengersPage(String gender) throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer passengersCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        List<LinkedHashMap<String, Object>> passengersMaps = JsonPath.parse(contentString).read("$.content", List.class);
        for (LinkedHashMap<String, Object> passengerMap : passengersMaps) {
            Assertions.assertEquals(gender, passengerMap.get("gender"));
        }
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(passengersCount > 0);
    }

    private void saveTestPassengers() throws Exception {
        Passenger testPassenger = Passenger.builder()
                .name("John")
                .email("john.c@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .phoneNumber("+323-322-243")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .build();

        Passenger testPassengerSecond = Passenger.builder()
                .name("Mattew")
                .email("mt.dt@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .phoneNumber("+323-322-111")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .build();
        savePassenger(testPassenger);
        savePassenger(testPassengerSecond);
    }

    private void savePassenger(Passenger passenger) throws Exception {
        PassengerRequestTO passengerRequest = passengerMapper.passengerToRequest(passenger);
        mockMvc.perform(post(PassengerTestLoader.URI)
                .content(objectMapper.writeValueAsString(passengerRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
