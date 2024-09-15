package com.modsen.software.rating.service;

import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.filter.RatingScoreFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RatingService {

    Page<RatingScoreResponseTO> getAllRatingScores(RatingScoreFilter filter, Pageable pageable);

    RatingScoreResponseTO findRatingScoreById(Long id);

    RatingScoreResponseTO saveRatingScore(RatingScoreRequestTO carRequest);

    RatingScoreResponseTO updateRatingScore(RatingScoreRequestTO carRequest);

    void deleteRatingScore(Long id);
}