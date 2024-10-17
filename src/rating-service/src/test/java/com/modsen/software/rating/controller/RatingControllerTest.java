package com.modsen.software.rating.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.rating.client.DriverClient;
import com.modsen.software.rating.client.PassengerClient;
import com.modsen.software.rating.dto.RatingEvaluationResponseTO;
import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.filter.RatingScoreFilter;
import com.modsen.software.rating.service.impl.RatingServiceImpl;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RatingController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RatingControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RatingServiceImpl ratingService;

    @MockBean
    private DriverClient driverClient;

    @MockBean
    private PassengerClient passengerClient;

    private RatingScore rating;

    private RatingScore secondRating;

    private RatingScoreRequestTO ratingRequest;

    private RatingScoreResponseTO ratingResponse;

    private RatingScoreResponseTO secondRatingResponse;

    @BeforeEach
    void setUpRatings() {
        rating = RatingScore.builder()
                .id(1L)
                .driverId(1L)
                .passengerId(1L)
                .evaluation(5)
                .initiator(Initiator.PASSENGER)
                .comment("Default comment")
                .build();
        secondRating = RatingScore.builder()
                .id(2L)
                .driverId(1L)
                .passengerId(1L)
                .evaluation(4)
                .initiator(Initiator.PASSENGER)
                .comment("Default comment 2")
                .build();

        ratingRequest = new RatingScoreRequestTO();
        ratingRequest.setId(1L);
        ratingRequest.setDriverId(1L);
        ratingRequest.setPassengerId(1L);
        ratingRequest.setEvaluation(5);
        ratingRequest.setInitiator(Initiator.PASSENGER);
        ratingRequest.setComment("Default comment");

        ratingResponse = new RatingScoreResponseTO();
        ratingResponse.setId(1L);
        ratingResponse.setDriverId(1L);
        ratingResponse.setPassengerId(1L);
        ratingResponse.setEvaluation(5);
        ratingResponse.setInitiator(Initiator.PASSENGER);
        ratingResponse.setComment("Default comment");

        secondRatingResponse = new RatingScoreResponseTO();
        secondRatingResponse.setId(2L);
        secondRatingResponse.setDriverId(1L);
        secondRatingResponse.setPassengerId(1L);
        secondRatingResponse.setEvaluation(4);
        secondRatingResponse.setInitiator(Initiator.PASSENGER);
        secondRatingResponse.setComment("Default comment 2");
    }

    @Test
    @Timeout(1000)
    void testUpdateRating() throws Exception {

        RatingScoreRequestTO ratingUpdateRequest = new RatingScoreRequestTO();
        ratingUpdateRequest.setId(1L);
        ratingUpdateRequest.setDriverId(1L);
        ratingUpdateRequest.setPassengerId(1L);
        ratingUpdateRequest.setEvaluation(4);
        ratingUpdateRequest.setInitiator(Initiator.PASSENGER);
        ratingUpdateRequest.setComment("Default comment");

        RatingScoreResponseTO ratingUpdateResponse = new RatingScoreResponseTO();
        ratingUpdateResponse.setId(1L);
        ratingUpdateResponse.setDriverId(1L);
        ratingUpdateResponse.setPassengerId(1L);
        ratingUpdateResponse.setEvaluation(4);
        ratingUpdateResponse.setInitiator(Initiator.PASSENGER);
        ratingUpdateResponse.setComment("Default comment");

        ObjectMapper mapper = new ObjectMapper();
        when(ratingService.updateRatingScore(ratingUpdateRequest)).thenReturn(ratingUpdateResponse);
        mockMvc.perform(put("/api/v1/scores")
                        .content(mapper.writeValueAsString(ratingUpdateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.passengerId").value(1));
        verify(ratingService, times(1)).updateRatingScore(ratingUpdateRequest);
    }

    @Test
    @Timeout(1000)
    void testRatingEvaluation() throws Exception {
        when(ratingService.evaluateMeanRatingById(anyLong(), any(Initiator.class), any(Pageable.class)))
                .thenAnswer(invocationOnMock -> new RatingEvaluationResponseTO(invocationOnMock.getArgument(0), BigDecimal.valueOf(5)));
        mockMvc.perform(get("/api/v1/scores/evaluate/{id}", 1L)
                        .param("initiator", Initiator.PASSENGER.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.meanEvaluation").value(BigDecimal.valueOf(5)));
        verify(ratingService, times(1)).evaluateMeanRatingById(eq(1L), any(Initiator.class), any(Pageable.class));
    }

    @Test
    @Timeout(1000)
    void testCreateRating() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(ratingService.saveRatingScore(ratingRequest)).thenReturn(ratingResponse);
        mockMvc.perform(post("/api/v1/scores")
                        .content(mapper.writeValueAsString(ratingRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.passengerId").value(1));
        verify(ratingService, times(1)).saveRatingScore(ratingRequest);
    }

    @Test
    @Timeout(1000)
    void testDeleteRating() throws Exception {
        mockMvc.perform(delete("/api/v1/scores/{id}", ratingRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));

        verify(ratingService, times(1)).deleteRatingScore(ratingRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testFindRatingById() throws Exception {
        when(ratingService.findRatingScoreById(ratingRequest.getId())).thenReturn(ratingResponse);
        mockMvc.perform(get("/api/v1/scores/{id}", ratingRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.passengerId").value(1));
        verify(ratingService, times(1)).findRatingScoreById(ratingRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testGetAllRatings() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
        ArgumentCaptor<RatingScoreFilter> filterCaptor = forClass(RatingScoreFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = forClass(Pageable.class);
        when(ratingService.getAllRatingScores(any(RatingScoreFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Stream.of(ratingResponse, secondRatingResponse).toList(),
                        pageable, 2));
        mockMvc.perform(get("/api/v1/scores")
                        .param("size", "10")
                        .param("page", "0")
                        .param("sort", "id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].driverId").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
        verify(ratingService, times(1)).getAllRatingScores(filterCaptor.capture(),
                pageableCaptor.capture());
    }
}
