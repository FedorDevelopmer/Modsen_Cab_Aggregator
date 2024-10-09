package com.modsen.software.ride.component_testing.ride_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.mapper.RideMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RideSaveTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private RideMapper rideMapper;

    @Autowired
    private RestClient restClient;

    @Autowired
    private ObjectMapper objectMapper;

    @When("I save new ride with driver id = {long}, passenger id = {long} and price = {double}")
    public void saveNewRide(Long driverId, Long passengerId, Double price) throws Exception {
        Ride ride = Ride.builder()
                .driverId(driverId)
                .passengerId(passengerId)
                .departureAddress("Minsk,Shirayeva,145a")
                .destinationAddress("Minsk,Golybeva,9")
                .ridePrice(BigDecimal.valueOf(price))
                .rideOrderTime(LocalDateTime.now())
                .rideStatus(RideStatus.CREATED)
                .build();

        result = mockMvc.perform(post(RideTestLoader.URI)
                .content(objectMapper.writeValueAsString(rideMapper.rideToRequest(ride)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("saved ride with driver id = {long}, passenger id = {long} and price = {double} should be returned")
    public void responseContainsSavedRide(Long driverId, Long passengerId, Double price) throws Exception {
        String rideString = result.getResponse().getContentAsString();
        Ride rideScore = objectMapper.readValue(rideString, Ride.class);
        Assertions.assertEquals(driverId, rideScore.getDriverId());
        Assertions.assertEquals(passengerId, rideScore.getPassengerId());
        Assertions.assertEquals(BigDecimal.valueOf(price).setScale(2), rideScore.getRidePrice().setScale(2));
    }
}
