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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RatingDeleteTestSteps {
    @Autowired
    private MockMvc mockMvc;

    private MvcResult result;

    @Autowired
    private RatingScoreMapper ratingScoreMapper;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I try to delete rating score with id = {long}")
    public void ratingDeleteRequest(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RatingTestLoader.RATING_SCORES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRating();
        result = mockMvc.perform(delete(RatingTestLoader.URI + "/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("the response should indicate successful delete of rating score with code {int}\\(NO_CONTENT)")
    public void responseIndicatesSuccessfulDelete(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("request complete with code {int}\\(NOT_FOUND) and indicates that specified for delete rating score not found")
    public void responseCodeIsNotFound(Integer code) {
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

    private void saveRating(RatingScore ratingScore) throws Exception {
        RatingScoreRequestTO ratingScoreRequest = ratingScoreMapper.ratingScoreToRequest(ratingScore);
        mockMvc.perform(post(RatingTestLoader.URI)
                .content(objectMapper.writeValueAsString(ratingScoreRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
