package com.modsen.software.rating.component_testing.rating_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.mapper.RatingScoreMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.util.LinkedHashMap;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class RatingFindAllTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RatingScoreMapper ratingScoreMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request all rating scores from database through service")
    public void requestAllRatings() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RatingTestLoader.RATING_SCORES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRatings();
        result = mockMvc.perform(get(RatingTestLoader.URI).contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("all rating scores request complete with code {int} \\(OK)")
    public void allRatingsCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("all rating scores filtered by driver id request complete with code {int} \\(OK)")
    public void allMalesResponseCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @And("first rating scores page should be returned")
    public void responseContainRatingsPage() throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer ratingScoresCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(ratingScoresCount > 0);
    }

    @When("I request all rating scores with driver id = {long} from database through service")
    public void requestAllMaleRatings(Long id) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + RatingTestLoader.RATING_SCORES_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestRatings();
        result = mockMvc.perform(get(RatingTestLoader.URI)
                .param("driverId", id.toString())
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @And("first page of filtered by driver id = {long} rating scores should be returned")
    public void responseContainMaleRatingsPage(Long driverId) throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer ratingScoresCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        List<LinkedHashMap<String, Object>> driversMaps = JsonPath.parse(contentString).read("$.content", List.class);
        for (LinkedHashMap<String, Object> driverMap : driversMaps) {
            Assertions.assertEquals(driverId, Long.parseLong(driverMap.get("driverId").toString()));
        }
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(ratingScoresCount > 0);
    }

    private void saveTestRatings() throws Exception {
        RatingScore testRating = RatingScore.builder()
                .driverId(1L)
                .passengerId(1L)
                .evaluation(5)
                .initiator(Initiator.DRIVER)
                .build();

        RatingScore testRatingSecond = RatingScore.builder()
                .driverId(2L)
                .passengerId(2L)
                .evaluation(5)
                .initiator(Initiator.PASSENGER)
                .build();
        saveRating(testRating);
        saveRating(testRatingSecond);
    }

    private void saveRating(RatingScore rating) throws Exception {
        RatingScoreRequestTO ratingRequest = ratingScoreMapper.ratingScoreToRequest(rating);
        mockMvc.perform(post(RatingTestLoader.URI)
                .content(objectMapper.writeValueAsString(ratingRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
