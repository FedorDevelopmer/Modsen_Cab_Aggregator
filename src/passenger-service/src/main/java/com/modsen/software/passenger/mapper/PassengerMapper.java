package com.modsen.software.passenger.mapper;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PassengerMapper {
    PassengerResponseTO passengerToResponse(Passenger driver);

    Passenger responseToPassenger(PassengerResponseTO driverResponseTo);

    PassengerRequestTO passengerToRequest(Passenger driver);

    Passenger requestToPassenger(PassengerRequestTO driverRequestTo);
}
