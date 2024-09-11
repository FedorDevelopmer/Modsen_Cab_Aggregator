package com.modsen.software.ride.service;

import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;

import java.util.List;

public interface RideService {

    List<RideResponseTO> getAllRides(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    RideResponseTO findRideById(Long id);
    RideResponseTO saveRide(RideRequestTO carRequest);
    RideResponseTO updateRide(RideRequestTO carRequest);
    RideResponseTO updateRideStatus(Long id,String status);
    void deleteRide(Long id);

}
