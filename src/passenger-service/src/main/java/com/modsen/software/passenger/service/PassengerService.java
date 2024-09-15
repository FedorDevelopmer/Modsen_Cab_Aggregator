package com.modsen.software.passenger.service;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.filter.PassengerFilter;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface PassengerService {

    List<PassengerResponseTO> getAllPassengers(PassengerFilter filter, Pageable pageable);

    PassengerResponseTO findPassengerById(Long id);

    PassengerResponseTO savePassenger(PassengerRequestTO carRequest);

    PassengerResponseTO updatePassenger(PassengerRequestTO carRequest);

    void deletePassenger(Long id);

}
