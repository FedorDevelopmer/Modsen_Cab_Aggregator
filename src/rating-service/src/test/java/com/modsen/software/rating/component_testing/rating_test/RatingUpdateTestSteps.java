package com.modsen.software.rating.component_testing.rating_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.mapper.RatingScoreMapper;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

public class RatingUpdateTestSteps {

    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private RatingScoreMapper ratingScoreMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to update rating score with id = {long} changing evaluation to {int}")
    public void ratingUpdateRequest(Long id, Integer evaluation) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RatingTestLoader.RATING_SCORES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRating();
        RatingScore rating = RatingScore.builder()
                .id(id)
                .driverId(1L)
                .passengerId(1L)
                .evaluation(evaluation)
                .initiator(Initiator.DRIVER)
                .build();

        result = mockMvc.perform(put(RatingTestLoader.URI)
                .content(objectMapper.writeValueAsString(ratingScoreMapper.ratingScoreToRequest(rating)))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("updated rating with evaluation = {int} should be returned")
    public void responseContainsSavedRating(Integer evaluation) throws Exception {
        String ratingString = result.getResponse().getContentAsString();
        RatingScore ratingScore = objectMapper.readValue(ratingString, RatingScore.class);
        Assertions.assertEquals(evaluation, ratingScore.getEvaluation());
    }

    private void saveTestRating() throws Exception {
        RatingScore testRatingScore = RatingScore.builder()
                .driverId(1L)
                .passengerId(1L)
                .evaluation(5)
                .initiator(Initiator.DRIVER)
                .build();
        saveRatingScore(testRatingScore);
    }

    private void saveRatingScore(RatingScore rating) throws Exception {
        RatingScoreRequestTO ratingScoreRequest = ratingScoreMapper.ratingScoreToRequest(rating);
        mockMvc.perform(post(RatingTestLoader.URI)
                .content(objectMapper.writeValueAsString(ratingScoreRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
