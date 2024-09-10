package com.modsen.software.ride.mapper;

import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.Ride;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RideMapper {
    RideResponseTO rideToResponse(Ride driver);

    Ride responseToRide(RideResponseTO driverResponseTo);

    RideRequestTO rideToRequest(Ride driver);

    Ride requestToRide(RideRequestTO driverRequestTo);
}
