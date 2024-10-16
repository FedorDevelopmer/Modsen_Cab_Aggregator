package com.modsen.software.ride.component_testing.ride_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.mapper.RideMapper;
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

public class RideUpdateTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private RideMapper rideMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to update ride with id = {long} changing price to {double}")
    public void rideUpdateRequest(Long id, Double price) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RideTestLoader.RIDES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRide();
        Ride ride = Ride.builder()
                .id(id)
                .driverId(1L)
                .passengerId(1L)
                .departureAddress("Minsk,Shirayeva,145a")
                .destinationAddress("Minsk,Golybeva,9")
                .ridePrice(BigDecimal.valueOf(price))
                .rideOrderTime(LocalDateTime.now())
                .rideStatus(RideStatus.CREATED)
                .build();

        result = mockMvc.perform(put(RideTestLoader.URI)
                .content(objectMapper.writeValueAsString(rideMapper.rideToRequest(ride)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("updated ride with price = {double} should be returned")
    public void responseContainsSavedRide(Double price) throws Exception {
        String rideString = result.getResponse().getContentAsString();
        Ride rideScore = objectMapper.readValue(rideString, Ride.class);
        Assertions.assertEquals(BigDecimal.valueOf(price), rideScore.getRidePrice());
    }

    @When("I try to change status of ride with id = {long} to {string}")
    public void rideUpdateRequest(Long id, String updatedStatus) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RideTestLoader.RIDES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRide();
        result = mockMvc.perform(put(RideTestLoader.URI + "/status/{id}", id)
                .param("status", updatedStatus)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("ride with changed status {string} should be returned")
    public void responseContainsSavedRide(String status) throws Exception {
        String rideString = result.getResponse().getContentAsString();
        Ride rideScore = objectMapper.readValue(rideString, Ride.class);
        Assertions.assertEquals(status, rideScore.getRideStatus().name());
    }

    private void saveTestRide() throws Exception {
        Ride testRide = Ride.builder()
                .driverId(1L)
                .passengerId(1L)
                .departureAddress("Minsk,Shirayeva,145a")
                .destinationAddress("Minsk,Golybeva,9")
                .ridePrice(BigDecimal.valueOf(15))
                .rideOrderTime(LocalDateTime.now())
                .rideStatus(RideStatus.CREATED)
                .build();
        saveRide(testRide);
    }

    private void saveRide(Ride ride) throws Exception {
        RideRequestTO rideScoreRequest = rideMapper.rideToRequest(ride);
        mockMvc.perform(post(RideTestLoader.URI)
                .content(objectMapper.writeValueAsString(rideScoreRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
