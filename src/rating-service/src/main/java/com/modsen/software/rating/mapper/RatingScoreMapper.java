package com.modsen.software.rating.mapper;

import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.entity.RatingScore;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingScoreMapper {
    RatingScoreResponseTO ratingScoreToResponse(RatingScore driver);

    RatingScore responseToRatingScore(RatingScoreResponseTO driverResponseTo);

    RatingScoreRequestTO ratingScoreToRequest(RatingScore driver);

    RatingScore requestToRatingScore(RatingScoreRequestTO driverRequestTo);
}
