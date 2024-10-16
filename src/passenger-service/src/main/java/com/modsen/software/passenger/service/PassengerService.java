package com.modsen.software.passenger.service;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.dto.RatingEvaluationResponseTO;
import com.modsen.software.passenger.filter.PassengerFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PassengerService {

    Page<PassengerResponseTO> getAllPassengers(PassengerFilter filter, Pageable pageable);

    PassengerResponseTO findPassengerById(Long id);

    PassengerResponseTO savePassenger(PassengerRequestTO carRequest);

    PassengerResponseTO updatePassenger(PassengerRequestTO carRequest);

    void updatePassengerByKafka(RatingEvaluationResponseTO ratingEvaluation);

    void deletePassenger(Long id);

}
