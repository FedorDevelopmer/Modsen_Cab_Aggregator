package com.modsen.software.rating.repository;

import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.filter.RatingScoreFilter;
import com.modsen.software.rating.specification.RatingScoreSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RatingRepositoryUnitTest {

    @Autowired
    private RatingRepository repository;

    private RatingScore rating;

    private RatingScore secondRating;

    private RatingScoreFilter filter;

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
    void testSaveRating() {
        RatingScore savedRating = repository.save(rating);
        Assertions.assertNotNull(rating);
        Assertions.assertEquals(1L, savedRating.getId());
        Assertions.assertEquals(1L, savedRating.getDriverId());
        Assertions.assertEquals(5, savedRating.getEvaluation());
    }

    @Test
    void testUpdateRating() {
        repository.save(rating);
        rating.setEvaluation(4);
        rating.setDriverId(2L);
        RatingScore updatedRating = repository.save(rating);
        Assertions.assertNotNull(updatedRating);
        Assertions.assertEquals(4, updatedRating.getEvaluation());
        Assertions.assertEquals(2L, updatedRating.getDriverId());
    }

    @Test
    void testFindById() {
        repository.save(rating);
        Optional<RatingScore> foundRating = repository.findById(rating.getId());
        Assertions.assertNotEquals(Optional.empty(), foundRating);
        Assertions.assertEquals(1L, foundRating.get().getDriverId());
    }

    @Test
    void testFindAll() {
        repository.save(rating);
        repository.save(secondRating);
        List<RatingScore> scores = repository.findAll();
        Assertions.assertNotNull(scores);
        Assertions.assertEquals(2, scores.size());
        Assertions.assertEquals(rating.getId(), scores.get(0).getId());
        Assertions.assertEquals(secondRating.getId(), scores.get(1).getId());
    }

    @Test
    void testFindAllWithFilterByGender() {
        repository.save(rating);
        repository.save(secondRating);
        filter = new RatingScoreFilter();
        filter.setPassengerId(2L);
        Specification<RatingScore> spec = Specification.where(RatingScoreSpecification.hasPassengerId(filter.getPassengerId()));
        List<RatingScore> scores = repository.findAll(spec);
        Assertions.assertNotNull(scores);
        Assertions.assertEquals(1, scores.size());
        Assertions.assertEquals(secondRating.getId(), scores.get(0).getId());
    }
}
