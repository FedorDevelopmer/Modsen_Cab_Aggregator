package com.modsen.software.passenger.service;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;

import java.util.List;

public interface PassengerService {

    List<PassengerResponseTO> getAllPassengers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    PassengerResponseTO findPassengerById(Long id);

    PassengerResponseTO savePassenger(PassengerRequestTO carRequest);

    PassengerResponseTO updatePassenger(PassengerRequestTO carRequest);

    void deletePassenger(Long id);

}
