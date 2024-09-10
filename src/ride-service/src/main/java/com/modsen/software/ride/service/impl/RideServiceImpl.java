package com.modsen.software.ride.service.impl;

import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.Ride;
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
        return saveRide(rideTO);
    }

    @Transactional
    public RideResponseTO saveRide(RideRequestTO rideTO) {
        rideTO.setId(null);
        Ride saved = repository.save(mapper.requestToRide(rideTO));
        return mapper.rideToResponse(saved);
    }

    @Transactional
    public void deleteRide(Long id) {
        repository.delete(mapper.responseToRide(findRideById(id)));
    }

}
