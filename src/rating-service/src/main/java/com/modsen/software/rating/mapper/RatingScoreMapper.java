package com.modsen.software.rating.mapper;

import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.entity.RatingScore;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RatingScoreMapper {
    RatingScoreResponseTO ratingToResponse(RatingScore driver);

    RatingScore responseToRide(RatingScoreResponseTO driverResponseTo);

    RatingScoreRequestTO ratingToRequest(RatingScore driver);

    RatingScore requestToRide(RatingScoreRequestTO driverRequestTo);
}
