package com.modsen.software.passenger.component_testing.passenger_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.mapper.PassengerMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PassengerSaveTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private PassengerMapper passengerMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @When("I save new passenger with name {string}, email {string} and phone number {string}")
    public void saveNewPassenger(String name, String email, String phone) throws Exception {
        Passenger passenger = Passenger.builder()
                .name(name)
                .email(email)
                .rating(BigDecimal.valueOf(5))
                .phoneNumber(phone)
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .build();

        result = mockMvc.perform(post(PassengerTestLoader.URI)
                .content(objectMapper.writeValueAsString(passengerMapper.passengerToRequest(passenger)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("saved passenger with name {string}, email {string}, phone number {string} should be returned")
    public void responseContainsSavedPassenger(String name, String email, String phone) throws Exception {
        String passengerString = result.getResponse().getContentAsString();
        Passenger passenger = objectMapper.readValue(passengerString, Passenger.class);
        Assertions.assertEquals(name, passenger.getName());
        Assertions.assertEquals(email, passenger.getEmail());
        Assertions.assertEquals(phone, passenger.getPhoneNumber());
    }

    @Then("the response should indicate with code {int} that email already owned by another passenger")
    public void responseIndicatesDuplicateEmail(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }
}
