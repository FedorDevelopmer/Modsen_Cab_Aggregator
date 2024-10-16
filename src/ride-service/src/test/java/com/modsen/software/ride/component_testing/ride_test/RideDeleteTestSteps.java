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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RideDeleteTestSteps {
    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private RideMapper rideMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to delete ride with id = {long}")
    public void rideDeleteRequest(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RideTestLoader.RIDES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRide();
        result = mockMvc.perform(delete(RideTestLoader.URI + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("the response should indicate successful delete of ride with code {int}\\(NO_CONTENT)")
    public void responseIndicatesSuccessfulDelete(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified for delete ride not found")
    public void responseCodeIsNotFound(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
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

    private void saveRide(Ride rideScore) throws Exception {
        RideRequestTO rideScoreRequest = rideMapper.rideToRequest(rideScore);
        mockMvc.perform(post(RideTestLoader.URI)
                .content(objectMapper.writeValueAsString(rideScoreRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
