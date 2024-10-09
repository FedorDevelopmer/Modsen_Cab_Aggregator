package com.modsen.software.ride.component_testing.ride_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.mapper.RideMapper;
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

public class RideFindAllTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RideMapper rideMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request all rides from database through service")
    public void requestAllRides() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RideTestLoader.RIDES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRides();
        result = mockMvc.perform(get(RideTestLoader.URI).contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("all rides request complete with code {int} \\(OK)")
    public void allRidesCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("all rides filtered by passenger id request complete with code {int} \\(OK)")
    public void allMalesResponseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @And("first rides page should be returned")
    public void responseContainRidesPage() throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer rideScoresCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(rideScoresCount > 0);
    }

    @When("I request all rides with passenger id = {long} from database through service")
    public void requestAllMaleRides(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RideTestLoader.RIDES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRides();
        result = mockMvc.perform(get(RideTestLoader.URI)
                .param("driverId", id.toString())
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @And("first page of filtered by passenger id = {long} rides should be returned")
    public void responseContainMaleRidesPage(Long passengerId) throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer rideScoresCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        List<LinkedHashMap<String, Object>> ridesMaps = JsonPath.parse(contentString).read("$.content", List.class);
        for (LinkedHashMap<String, Object> driverMap : ridesMaps) {
            Assertions.assertEquals(passengerId, Long.parseLong(driverMap.get("passengerId").toString()));
        }
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(rideScoresCount > 0);
    }

    private void saveTestRides() throws Exception {
        Ride testRide = Ride.builder()
                .driverId(1L)
                .passengerId(1L)
                .departureAddress("Minsk,Shirayeva,145a")
                .destinationAddress("Minsk,Golybeva,9")
                .ridePrice(BigDecimal.valueOf(15))
                .rideOrderTime(LocalDateTime.now())
                .rideStatus(RideStatus.CREATED)
                .build();

        Ride testRideSecond = Ride.builder()
                .driverId(2L)
                .passengerId(2L)
                .departureAddress("Minsk,Bricketa,114")
                .destinationAddress("Minsk,Vaneeva,9")
                .ridePrice(BigDecimal.valueOf(25))
                .rideOrderTime(LocalDateTime.now())
                .rideStatus(RideStatus.ACCEPTED)
                .build();
        saveRide(testRide);
        saveRide(testRideSecond);
    }

    private void saveRide(Ride ride) throws Exception {
        RideRequestTO rideRequest = rideMapper.rideToRequest(ride);
        mockMvc.perform(post(RideTestLoader.URI)
                .content(objectMapper.writeValueAsString(rideRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
