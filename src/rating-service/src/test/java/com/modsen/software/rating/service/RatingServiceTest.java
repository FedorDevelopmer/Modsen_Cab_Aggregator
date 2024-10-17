package com.modsen.software.rating.service;

import com.modsen.software.rating.client.DriverClient;
import com.modsen.software.rating.client.PassengerClient;
import com.modsen.software.rating.dto.*;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.filter.RatingScoreFilter;
import com.modsen.software.rating.repository.RatingRepository;
import com.modsen.software.rating.service.impl.RatingServiceImpl;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.InjectMocks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RatingServiceTest {

    @MockBean
    private RatingRepository ratingRepository;

    @MockBean
    private DriverClient driverClient;

    @MockBean
    private PassengerClient passengerClient;

    @InjectMocks
    @Autowired
    private RatingServiceImpl ratingService;

    private RatingScore rating;

    private RatingScore secondRating;

    @BeforeEach
    void setUpRating() {
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
                .passengerId(2L)
                .evaluation(4)
                .initiator(Initiator.PASSENGER)
                .comment("Default comment 2")
                .build();
    }

    @Test
    @Timeout(1000)
    void testSaveRating() {
        RatingScoreRequestTO ratingRequest = new RatingScoreRequestTO(1L, 1L,
                1L, 5,
                "Default comment", Initiator.PASSENGER);
        when(ratingRepository.save(any(RatingScore.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(driverClient.getDriver(anyLong())).thenReturn(new DriverResponseTO());
        when(passengerClient.getPassenger(anyLong())).thenReturn(new PassengerResponseTO());
        RatingScoreResponseTO savedRating = ratingService.saveRatingScore(ratingRequest);
        assertNotNull(savedRating);
        assertEquals(rating.getId(), savedRating.getId());
        Assertions.assertEquals(1L, savedRating.getDriverId());
        Assertions.assertEquals(1L, savedRating.getPassengerId());
    }

    @Test
    @Timeout(1000)
    void testEvaluateMeanRating() {
        Pageable pageable = PageRequest.of(0, 50);
        when(ratingRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Stream.of(rating, secondRating).toList(),
                        pageable, 2));
        RatingEvaluationResponseTO evaluationOfScores = ratingService.evaluateMeanRatingById(1L, Initiator.PASSENGER, pageable);
        assertNotNull(evaluationOfScores);
        assertEquals(1L, evaluationOfScores.getId());
        Assertions.assertEquals(BigDecimal.valueOf(4.98), evaluationOfScores.getMeanEvaluation());
    }

    @Test
    @Timeout(1000)
    void testUpdateRating() {
        RatingScoreRequestTO ratingRequest = new RatingScoreRequestTO(1L, 1L,
                1L, 5,
                "Default comment", Initiator.PASSENGER);
        RatingScoreRequestTO ratingUpdateRequest = new RatingScoreRequestTO(1L, 1L,
                1L, 4,
                "Default comment", Initiator.PASSENGER);
        when(ratingRepository.save(any(RatingScore.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(ratingRepository.findById(rating.getId())).thenReturn(Optional.of(rating));
        ratingService.saveRatingScore(ratingRequest);
        RatingScoreResponseTO updatedRating = ratingService.updateRatingScore(ratingUpdateRequest);
        assertNotNull(updatedRating);
        assertEquals(rating.getId(), updatedRating.getId());
        assertEquals(4, updatedRating.getEvaluation());
    }

    @Test
    @Timeout(1000)
    void testDeleteRating() {
        RatingScoreRequestTO ratingRequest = new RatingScoreRequestTO(1L, 1L,
                1L, 5,
                "Default comment", Initiator.PASSENGER);
        when(ratingRepository.save(any(RatingScore.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(ratingRepository.findById(rating.getId())).thenReturn(Optional.of(rating));
        RatingScoreResponseTO savedRating = ratingService.saveRatingScore(ratingRequest);
        ratingService.deleteRatingScore(savedRating.getId());
    }

    @Test
    @Timeout(1000)
    void testFindRatingById() {
        RatingScoreRequestTO ratingRequest = new RatingScoreRequestTO(1L, 1L,
                1L, 5,
                "Default comment", Initiator.PASSENGER);
        when(ratingRepository.save(any(RatingScore.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(ratingRepository.findById(rating.getId())).thenReturn(Optional.of(rating));
        ratingService.saveRatingScore(ratingRequest);
        RatingScoreResponseTO result = ratingService.findRatingScoreById(rating.getId());
        assertNotNull(result);
        assertEquals(rating.getId(), result.getId());
        Assertions.assertEquals(1L, rating.getDriverId());
        Assertions.assertEquals(1L, rating.getPassengerId());
    }

    @Test
    @Timeout(1000)
    void testFindAllRatings() {
        RatingScoreFilter filter = new RatingScoreFilter();
        Pageable pageable = PageRequest.of(0, 10);
        when(ratingRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(Stream.of(rating, secondRating).toList(), pageable, 2));
        Page<RatingScoreResponseTO> ratingsList = ratingService.getAllRatingScores(filter, pageable);
        assertNotNull(ratingsList);
        assertEquals(2L, ratingsList.getTotalElements());
        assertEquals(rating.getId(), ratingsList.getContent().get(0).getId());
        assertEquals(secondRating.getId(), ratingsList.getContent().get(1).getId());
        assertTrue(ratingsList.isFirst());
        assertEquals(10, ratingsList.getSize());
    }
}
