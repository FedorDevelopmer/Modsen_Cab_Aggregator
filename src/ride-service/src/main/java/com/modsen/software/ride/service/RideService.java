package com.modsen.software.ride.service;

import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.filter.RideFilter;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RideService {

    List<RideResponseTO> getAllRides(RideFilter filter, Pageable pageable);

    RideResponseTO findRideById(Long id);

    RideResponseTO saveRide(RideRequestTO carRequest);

    RideResponseTO updateRide(RideRequestTO carRequest);

    RideResponseTO updateRideStatus(Long id, RideStatus status);

    void deleteRide(Long id);
}
