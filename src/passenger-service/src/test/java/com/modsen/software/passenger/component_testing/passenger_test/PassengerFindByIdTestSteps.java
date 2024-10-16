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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class PassengerFindByIdTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PassengerMapper passengerMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request passenger with id = {long} from database through service")
    public void requestPassengerById(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + PassengerTestLoader.PASSENGERS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestPassenger();
        result = mockMvc.perform(get(PassengerTestLoader.URI + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Then("find by id request complete with code {int} \\(OK) for passenger")
    public void responseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("returned passenger must be with name {string}, email {string} and phone number {string}")
    public void responseContainsSavedPassenger(String name, String email, String phone) throws Exception {
        String passengerString = result.getResponse().getContentAsString();
        Passenger passenger = objectMapper.readValue(passengerString, Passenger.class);
        Assertions.assertEquals(name, passenger.getName());
        Assertions.assertEquals(email, passenger.getEmail());
        Assertions.assertEquals(phone, passenger.getPhoneNumber());
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified passenger not found")
    public void responseCodeIsNotFound(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestPassenger() throws Exception {
        Passenger testPassenger = Passenger.builder()
                .name("Andrew")
                .email("a.sp@mail.com")
                .rating(BigDecimal.valueOf(5))
                .phoneNumber("+909-987-324")
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
