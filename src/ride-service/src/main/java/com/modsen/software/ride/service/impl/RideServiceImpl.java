package com.modsen.software.ride.service.impl;

import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.exception.RideNotFoundException;
import com.modsen.software.ride.mapper.RideMapper;
import com.modsen.software.ride.repository.RideRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RideServiceImpl {
    @Autowired
    private RideRepository repository;

    @Autowired
    private RideMapper mapper;

    @Transactional
    public List<RideResponseTO> getAllRides(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return repository.findAll(PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Direction.valueOf(sortOrder), sortBy)))
                .stream()
                .map(mapper::rideToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RideResponseTO findRideById(Long id) {
        Optional<Ride> ride = repository.findById(id);
        return mapper.rideToResponse(ride.orElseThrow(RideNotFoundException::new));
    }

    @Transactional
    public RideResponseTO updateRide(RideRequestTO rideTO) {
        repository.findById(rideTO.getId()).orElseThrow(RideNotFoundException::new);
        return mapper.rideToResponse(repository.save(mapper.requestToRide(rideTO)));
    }

    @Transactional
    public RideResponseTO updateRideStatus(Long id, RideStatus status) {
        Ride ride = repository.findById(id).orElseThrow(RideNotFoundException::new);
        ride.setRideStatus(status);
        return mapper.rideToResponse(repository.save(ride));
    }

    @Transactional
    public RideResponseTO saveRide(RideRequestTO rideTO) {
        return mapper.rideToResponse(repository.save(mapper.requestToRide(rideTO)));
    }

    @Transactional
    public void deleteRide(Long id) {
        repository.delete(mapper.responseToRide(findRideById(id)));
    }

}
