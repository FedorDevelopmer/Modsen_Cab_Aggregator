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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RatingFindByIdTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingScoreMapper ratingScoreMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request rating score with id = {long} from database through service")
    public void requestRatingById(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RatingTestLoader.RATING_SCORES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRating();
        result = mockMvc.perform(get(RatingTestLoader.URI + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andReturn();
    }

    @Then("find by id request complete with code {int} \\(OK) of rating scores")
    public void responseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("returned rating score must be with evaluation = {int} and initiator should be {string}")
    public void responseContainsSavedRating(Integer evaluation, String initiator) throws Exception {
        String ratingString = result.getResponse().getContentAsString();
        RatingScore ratingScore = objectMapper.readValue(ratingString, RatingScore.class);
        Assertions.assertEquals(evaluation, ratingScore.getEvaluation());
        Assertions.assertEquals(initiator, ratingScore.getInitiator().name());
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified rating score not found")
    public void responseCodeIsNotFound(int code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    private void saveTestRating() throws Exception {
        RatingScore testRating = RatingScore.builder()
                .driverId(1L)
                .passengerId(1L)
                .evaluation(5)
                .initiator(Initiator.DRIVER)
                .build();
        saveRating(testRating);
    }

    private void saveRating(RatingScore rating) throws Exception {
        RatingScoreRequestTO ratingRequest = ratingScoreMapper.ratingScoreToRequest(rating);
        mockMvc.perform(post(RatingTestLoader.URI)
                .content(objectMapper.writeValueAsString(ratingRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
