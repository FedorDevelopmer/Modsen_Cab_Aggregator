package com.modsen.software.passenger.component_testing.passenger_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.mapper.PassengerMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class PassengerUpdateTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private PassengerMapper passengerMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to update passenger with id = {long} changing name to {string} and email to {string}")
    public void passengerUpdateRequest(Long id, String name, String email) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + PassengerTestLoader.PASSENGERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestPassenger();
        Passenger passenger = Passenger.builder()
                .id(id)
                .name(name)
                .email(email)
                .rating(BigDecimal.valueOf(5))
                .phoneNumber("+111-111-111")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .build();

        result = mockMvc.perform(put(PassengerTestLoader.URI)
                .content(objectMapper.writeValueAsString(passengerMapper.passengerToRequest(passenger)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("updated passenger with name {string} and email {string} should be returned")
    public void responseContainsSavedPassenger(String name, String email) throws Exception {
        String passengerString = result.getResponse().getContentAsString();
        Passenger passenger = objectMapper.readValue(passengerString, Passenger.class);
        Assertions.assertEquals(name, passenger.getName());
        Assertions.assertEquals(email, passenger.getEmail());
    }

    @When("I try to update passenger with id = {long} changing email to {string}")
    public void passengerUpdateRequestWithDuplicateEmail(Long id, String email) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + PassengerTestLoader.PASSENGERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestPassenger();
        Passenger passenger = Passenger.builder()
                .name("John")
                .email(email)
                .rating(BigDecimal.valueOf(5))
                .phoneNumber("+222-222-222")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .build();
        savePassenger(passenger);
        Passenger passengerToUpdate = Passenger.builder()
                .id(id)
                .name("John")
                .email(email)
                .rating(BigDecimal.valueOf(5))
                .phoneNumber("+222-111-222")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .build();

        result = mockMvc.perform(put(PassengerTestLoader.URI)
                .content(objectMapper.writeValueAsString(passengerMapper.passengerToRequest(passengerToUpdate)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("the response should indicate with code {int} that updated email already owned by another passenger")
    public void responseIndicatesDuplicateEmail(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestPassenger() throws Exception {
        Passenger testPassenger = Passenger.builder()
                .name("Jack")
                .email("j.sp@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .phoneNumber("+243-424-98")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .build();
        savePassenger(testPassenger);
    }

    private void savePassenger(Passenger passenger) throws Exception {
        PassengerRequestTO driverRequest = passengerMapper.passengerToRequest(passenger);
        mockMvc.perform(post(PassengerTestLoader.URI)
                .content(objectMapper.writeValueAsString(driverRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
