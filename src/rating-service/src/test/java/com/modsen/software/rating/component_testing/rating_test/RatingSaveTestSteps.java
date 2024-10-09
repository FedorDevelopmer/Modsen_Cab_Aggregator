package com.modsen.software.rating.component_testing.rating_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.mapper.RatingScoreMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RatingSaveTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private RatingScoreMapper ratingScoreMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @When("I save new rating score with driver id = {long}, passenger id = {long} and evaluation = {int}")
    public void saveNewRating(Long driverId, Long passengerId, Integer evaluation) throws Exception {
        RatingScore rating = RatingScore.builder()
                .driverId(driverId)
                .passengerId(passengerId)
                .evaluation(evaluation)
                .initiator(Initiator.DRIVER)
                .build();

        result = mockMvc.perform(post(RatingTestLoader.URI)
                .content(objectMapper.writeValueAsString(ratingScoreMapper.ratingScoreToRequest(rating)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("saved rating score with driver id = {long}, passenger id = {long} and evaluation = {int} should be returned")
    public void responseContainsSavedRating(Long driverId, Long passengerId, Integer evaluation) throws Exception {
        String ratingString = result.getResponse().getContentAsString();
        RatingScore ratingScore = objectMapper.readValue(ratingString, RatingScore.class);
        Assertions.assertEquals(driverId, ratingScore.getDriverId());
        Assertions.assertEquals(passengerId, ratingScore.getPassengerId());
        Assertions.assertEquals(evaluation, ratingScore.getEvaluation());
    }
}
