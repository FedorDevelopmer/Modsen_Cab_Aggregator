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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PassengerSoftDeleteTestSteps {
    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private PassengerMapper passengerMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to delete passenger with id = {long}")
    public void passengerDeleteRequest(long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + PassengerTestLoader.PASSENGERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestPassenger();
        result = mockMvc.perform(delete(PassengerTestLoader.URI + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("the response should indicate successful delete of passenger with code {int}\\(NO_CONTENT)")
    public void responseIndicatesSuccessfulDelete(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified for delete passenger not found")
    public void responseCodeIsNotFound(Integer code) {
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
        PassengerRequestTO passengerRequest = passengerMapper.passengerToRequest(passenger);
        mockMvc.perform(post(PassengerTestLoader.URI)
                .content(objectMapper.writeValueAsString(passengerRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
