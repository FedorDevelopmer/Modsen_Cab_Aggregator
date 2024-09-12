package com.modsen.software.rating.service;

import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;

import java.util.List;

public interface RatingService {

    List<RatingScoreResponseTO> getAllRides(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    RatingScoreResponseTO findRideById(Long id);

    RatingScoreResponseTO saveRide(RatingScoreRequestTO carRequest);

    RatingScoreResponseTO updateRide(RatingScoreRequestTO carRequest);

    void deleteRide(Long id);

}