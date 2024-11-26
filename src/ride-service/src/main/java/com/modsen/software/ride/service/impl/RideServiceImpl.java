package com.modsen.software.ride.service.impl;

import com.modsen.software.ride.client.DriverClient;
import com.modsen.software.ride.client.PassengerClient;
import com.modsen.software.ride.dto.DriverResponseTO;
import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.exception.InvalidCredentialsException;
import com.modsen.software.ride.exception.RideNotFoundException;
import com.modsen.software.ride.filter.RideFilter;
import com.modsen.software.ride.mapper.RideMapper;
import com.modsen.software.ride.repository.RideRepository;
import com.modsen.software.ride.service.RideService;
import com.modsen.software.ride.specification.RideSpecification;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RideServiceImpl implements RideService {

    @Value("${jwt.auth.resource-id}")
    private String resourceId;

    @Autowired
    private DriverClient driverClient;

    @Autowired
    private PassengerClient passengerClient;

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
        driverClient.getDriver(rideTO.getDriverId());
        passengerClient.getPassenger(rideTO.getPassengerId());
        Ride savedRide = repository.save(mapper.requestToRide(rideTO));
        log.info("Update for ride with id {} complete successfully", rideTO.getId());
        return mapper.rideToResponse(savedRide);
    }

    @Transactional
    public RideResponseTO updateRideStatus(Long id, RideStatus status) {
        log.info("Updating ride status for entity with id {}, changing status to {}", id, status);
        Ride ride = repository.findById(id).orElseThrow(RideNotFoundException::new);
        String email = getUserEmail();
        DriverResponseTO driver = driverClient.getDriver(id);
        if ((driver.getEmail().equals(email) && !isAdmin()) || isAdmin()) {
            ride.setRideStatus(status);
            Ride savedRide = repository.save(ride);
            log.info("Status for ride with id {} successfully changed on {}", id, status);
            return mapper.rideToResponse(savedRide);
        } else {
            log.warn("User and driver emails during ride status update are different: user email: {}, passenger email {}", email, driver.getEmail());
            throw new InvalidCredentialsException("Driver email must be equal to email of user");
        }

    }

    @Transactional
    public RideResponseTO saveRide(RideRequestTO rideTO) {
        log.info("Saving new ride for driver with id {} and passenger with id {}",
                rideTO.getDriverId(), rideTO.getPassengerId());
        DriverResponseTO driver = driverClient.getDriver(rideTO.getDriverId());
        passengerClient.getPassenger(rideTO.getPassengerId());
        String email = getUserEmail();
        if ((driver.getEmail().equals(email) && !isAdmin()) || isAdmin()) {
            Ride savedRide = repository.save(mapper.requestToRide(rideTO));
            log.info("New ride with id {} saved successfully", savedRide.getId());
            return mapper.rideToResponse(savedRide);
        } else {
            log.warn("User and driver emails during ride creation are different: user email: {}, passenger email {}", email, driver.getEmail());
            throw new InvalidCredentialsException("Driver email must be equal to email of user");
        }
    }

    @Transactional
    public void deleteRide(Long id) {
        log.info("Deleting ride with id {}", id);
        repository.delete(mapper.responseToRide(findRideById(id)));
        log.info("Ride with id {} deleted successfully", id);
    }

    private String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = (Jwt) jwtAuth.getPrincipal();
            String email = jwt.getClaim("email").toString();
            if (email != null) {
                email = jwt.getClaim("email").toString();
            } else {
                log.warn("Field \"email\" is missed in JWT");
                throw new InvalidCredentialsException("Required field of JWT is missed: email");
            }
            return email;
        } else {
            log.warn("Unexpected authentication type during obtaining user email: expected JwtAuthenticationToken, but provided {}", authentication.getClass().getTypeName());
            throw new InvalidCredentialsException("Valid authentication type for application is JWT");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = (Jwt) jwtAuth.getPrincipal();
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            return Optional.ofNullable(resourceAccess)
                    .map(access -> (Map<String, Object>) access.get(resourceId))
                    .map(clientAccess -> (List<String>) clientAccess.get("roles"))
                    .map(roles -> {
                        if (roles.contains("Admin")) {
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .orElseThrow(() -> {
                        log.warn("Required field of JWT is missed: resource_access or {}", resourceId);
                        throw new InvalidCredentialsException("Format of JWT fields is incorrect for valid token");
                    });
        } else {
            log.warn("Unexpected authentication type during checking user role: expected JwtAuthenticationToken, but provided {}", authentication.getClass().getTypeName());
            throw new InvalidCredentialsException();
        }
    }
}
