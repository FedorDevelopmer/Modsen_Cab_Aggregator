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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RideFindByIdTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RideMapper rideMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request ride with id = {long} from database through service")
    public void requestRideById(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RideTestLoader.RIDES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRide();
        result = mockMvc.perform(get(RideTestLoader.URI + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Then("find by id request complete with code {int} \\(OK) of rides")
    public void responseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("returned ride must be with price = {double} and status {string}")
    public void responseContainsSavedRide(Double price, String status) throws Exception {
        String rideString = result.getResponse().getContentAsString();
        Ride ride = objectMapper.readValue(rideString, Ride.class);
        Assertions.assertEquals(status, ride.getRideStatus().name());
        Assertions.assertEquals(BigDecimal.valueOf(price).setScale(2), ride.getRidePrice().setScale(2));
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified ride not found")
    public void responseCodeIsNotFound(int code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestRide() throws Exception {
        Ride testRide = Ride.builder()
                .driverId(1L)
                .passengerId(1L)
                .departureAddress("Minsk,Shirayeva,145a")
                .destinationAddress("Minsk,Golybeva,9")
                .ridePrice(BigDecimal.valueOf(12.00))
                .rideOrderTime(LocalDateTime.now())
                .rideStatus(RideStatus.CREATED)
                .build();
        saveRide(testRide);
    }

    private void saveRide(Ride ride) throws Exception {
        RideRequestTO rideRequest = rideMapper.rideToRequest(ride);
        mockMvc.perform(post(RideTestLoader.URI)
                .content(objectMapper.writeValueAsString(rideRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
