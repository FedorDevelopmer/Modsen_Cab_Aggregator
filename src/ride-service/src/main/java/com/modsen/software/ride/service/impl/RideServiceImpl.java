package com.modsen.software.ride.service.impl;

import com.modsen.software.ride.dto.DriverResponseTO;
import com.modsen.software.ride.dto.PassengerResponseTO;
import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.exception.DriverNotFoundException;
import com.modsen.software.ride.exception.PassengerNotFoundException;
import com.modsen.software.ride.exception.RideNotFoundException;
import com.modsen.software.ride.filter.RideFilter;
import com.modsen.software.ride.mapper.RideMapper;
import com.modsen.software.ride.repository.RideRepository;
import com.modsen.software.ride.service.RideService;
import com.modsen.software.ride.specification.RideSpecification;
import jakarta.transaction.Transactional;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class RideServiceImpl implements RideService {

    private final String DRIVER_SERVICE_URI = "http://localhost:8080/api/v1/drivers";

    private final String PASSENGER_SERVICE_URI = "http://localhost:8081/api/v1/passengers";

    private RestClient client = RestClient.create();

    @Autowired
    private RideRepository repository;

    @Autowired
    private RideMapper mapper;

    @Transactional
    public Page<RideResponseTO> getAllRides(RideFilter filter, Pageable pageable) {
        log.info("Fetching rides page - page number: {},page size: {}, is first: {}",
                pageable.getPageNumber(), pageable.getPageSize(), !pageable.hasPrevious());
        Specification<Ride> spec = Specification.where(RideSpecification.hasDriverId(filter.getDriverId())
                .and(RideSpecification.hasPassengerId(filter.getPassengerId()))
                .and(RideSpecification.hasDepartureAddress(filter.getDepartureAddress()))
                .and(RideSpecification.hasDestinationAddress(filter.getDestinationAddress()))
                .and(RideSpecification.hasRideStatus(filter.getRideStatus()))
                .and(RideSpecification.hasRidePrice(filter.getRidePrice()))
                .and(RideSpecification.hasRidePriceLowerThan(filter.getRidePriceLower()))
                .and(RideSpecification.hasRidePriceHigherThan(filter.getRidePriceHigher()))
                .and(RideSpecification.hasRideOrderTime(filter.getRideOrderTime()))
                .and(RideSpecification.hasRideOrderTimeEarlier(filter.getRideOrderTimeEarlier()))
                .and(RideSpecification.hasRideOrderTimeLater(filter.getRideOrderTimeLater())));
        return repository.findAll(spec, pageable).map((item) -> mapper.rideToResponse(item));
    }

    @Transactional
    public RideResponseTO findRideById(Long id) {
        log.info("Fetching ride by id {}", id);
        Optional<Ride> ride = repository.findById(id);
        if (ride.isEmpty()) {
            log.warn("Ride with provided id {} not found", id);
        }
        return mapper.rideToResponse(ride.orElseThrow(RideNotFoundException::new));
    }

    @Transactional
    public RideResponseTO updateRide(RideRequestTO rideTO) {
        log.info("Updating ride with id {}", rideTO.getId());
        repository.findById(rideTO.getId()).orElseThrow(RideNotFoundException::new);
        getRideDriver(rideTO);
        getRidePassenger(rideTO);
        Ride savedRide = repository.save(mapper.requestToRide(rideTO));
        log.info("Update for ride with id {} complete successfully", rideTO.getId());
        return mapper.rideToResponse(savedRide);
    }

    @Transactional
    public RideResponseTO updateRideStatus(Long id, RideStatus status) {
        log.info("Updating ride status for entity with id {}, changing status to {}", id, status);
        Ride ride = repository.findById(id).orElseThrow(RideNotFoundException::new);
        ride.setRideStatus(status);
        Ride savedRide = repository.save(ride);
        log.info("Status for ride with id {} successfully changed on {}", id, status);
        return mapper.rideToResponse(savedRide);
    }

    @Transactional
    public RideResponseTO saveRide(RideRequestTO rideTO) {
        log.info("Saving new ride for driver with id {} and passenger with id {}",
                rideTO.getDriverId(), rideTO.getPassengerId());
        getRideDriver(rideTO);
        getRidePassenger(rideTO);
        Ride savedRide = repository.save(mapper.requestToRide(rideTO));
        log.info("New ride with id {} saved successfully", savedRide.getId());
        return mapper.rideToResponse(savedRide);
    }

    @Transactional
    public void deleteRide(Long id) {
        log.info("Deleting ride with id {}", id);
        repository.delete(mapper.responseToRide(findRideById(id)));
        log.info("Ride with id {} deleted successfully", id);
    }

    private void getRideDriver(RideRequestTO rideTO) {
        client.get()
                .uri(DRIVER_SERVICE_URI + "/{id}", rideTO.getDriverId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    log.warn("Driver with id {} not found for ride with id {}",
                            rideTO.getDriverId(), rideTO.getId());
                    throw new DriverNotFoundException();
                }))
                .body(DriverResponseTO.class);
    }

    private void getRidePassenger(RideRequestTO rideTO) {
        client.get()
                .uri(PASSENGER_SERVICE_URI + "/{id}", rideTO.getPassengerId())
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, ((request, response) -> {
                    log.warn("Passenger with id {} not found for ride with id {}",
                            rideTO.getPassengerId(), rideTO.getId());
                    throw new PassengerNotFoundException();
                }))
                .body(PassengerResponseTO.class);
    }
}
