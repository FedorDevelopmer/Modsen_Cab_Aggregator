package com.modsen.software.passenger.service.impl;

import com.modsen.software.passenger.client.RatingClient;
import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.dto.RatingEvaluationResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.exception.DuplicateEmailException;
import com.modsen.software.passenger.exception.DuplicatePhoneNumberException;
import com.modsen.software.passenger.exception.InvalidCredentialsException;
import com.modsen.software.passenger.exception.PassengerNotFoundException;
import com.modsen.software.passenger.filter.PassengerFilter;
import com.modsen.software.passenger.mapper.PassengerMapper;
import com.modsen.software.passenger.repository.PassengerRepository;
import com.modsen.software.passenger.service.PassengerService;
import com.modsen.software.passenger.specification.PassengerSpecification;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
public class PassengerServiceImpl implements PassengerService {

    @Value("${jwt.auth.resource-id}")
    private String resourceId;

    @Autowired
    private PassengerRepository repository;

    @Autowired
    private PassengerMapper mapper;

    @Autowired
    private RatingClient ratingClient;

    @Transactional
    public Page<PassengerResponseTO> getAllPassengers(PassengerFilter filter, Pageable pageable) {
        log.info("Fetching passengers page - page number: {},page size: {}, is first: {}",
                pageable.getPageNumber(), pageable.getPageSize(), !pageable.hasPrevious());
        Specification<Passenger> spec = Specification.where(PassengerSpecification.hasEmail(filter.getEmail()))
                .and(PassengerSpecification.hasName(filter.getName()))
                .and(PassengerSpecification.hasGender(filter.getGender()))
                .and(PassengerSpecification.hasPhone(filter.getPhoneNumber()))
                .and(PassengerSpecification.hasRemoveStatus(filter.getRemoveStatus()));
        return repository.findAll(spec, pageable)
                .map((item) -> {
                    if (!LocalDateTime.now().isBefore(item.getRatingUpdateTimestamp().plusDays(1))) {
                        updatePassengerDriverRating(item);
                    }
                    return item;
                })
                .map((item) -> mapper.passengerToResponse(item));
    }

    @Transactional
    public PassengerResponseTO findPassengerById(Long id) {
        log.info("Fetching passenger by id {}", id);
        Optional<Passenger> passengerOptional = repository.findById(id);
        if (passengerOptional.isEmpty()) {
            log.warn("Passenger with provided id {} not found", id);
            throw new PassengerNotFoundException();
        }
        Passenger passenger = passengerOptional.get();
        if (!LocalDateTime.now().isBefore(passenger.getRatingUpdateTimestamp().plusDays(1))) {
            updatePassengerDriverRating(passenger);
        }
        return mapper.passengerToResponse(passenger);
    }

    @Transactional
    public PassengerResponseTO updatePassenger(PassengerRequestTO passengerTO) {
        log.info("Updating passenger with id {}", passengerTO.getId());
        Optional<Passenger> oldPassengerOptional = repository.findById(passengerTO.getId());
        if (oldPassengerOptional.isEmpty()) {
            log.warn("Passenger for update with provided id {} not found", passengerTO.getId());
            throw new PassengerNotFoundException();
        }
        checkDuplications(passengerTO);
        String email = getUserEmail();
        Passenger oldPassenger = oldPassengerOptional.get();
        Passenger passengerToUpdate = mapper.requestToPassenger(passengerTO);
        if ((oldPassenger.getEmail().equals(email) && !isAdmin()) || isAdmin()) {
            passengerToUpdate.setRatingUpdateTimestamp(oldPassenger.getRatingUpdateTimestamp());
            passengerToUpdate.setRating(oldPassenger.getRating());
            if (!LocalDateTime.now().isBefore(oldPassenger.getRatingUpdateTimestamp().plusDays(1))) {
                Passenger updatedPassenger = updatePassengerDriverRating(passengerToUpdate);
                log.info("Update for passenger with id {} complete successfully(mean rating updated)", passengerTO.getId());
                return mapper.passengerToResponse(updatedPassenger);
            } else {
                Passenger updatedPassenger = repository.save(passengerToUpdate);
                log.info("Update for passenger with id {} complete successfully(mean rating not updated)", passengerTO.getId());
                return mapper.passengerToResponse(updatedPassenger);
            }
        } else {
            log.warn("User and entity email during passenger update are different: user email: {}, passenger email {}", email, passengerTO.getEmail());
            throw new InvalidCredentialsException("Passenger email must be equal to email of user");
        }
    }

    @Transactional
    public void updatePassengerByKafka(RatingEvaluationResponseTO ratingEvaluation) {
        log.info("Updating rating of passenger with id {} through Kafka", ratingEvaluation.getId());
        Passenger passenger = repository.findById(ratingEvaluation.getId()).orElseThrow(PassengerNotFoundException::new);
        passenger.setRating(ratingEvaluation.getMeanEvaluation());
        passenger.setRatingUpdateTimestamp(LocalDateTime.now());
        repository.save(passenger);
        log.info("Mean rating for passenger with id {} is updated through Kafka", ratingEvaluation.getId());
    }

    @Transactional
    public PassengerResponseTO savePassenger(PassengerRequestTO passengerTO) {
        log.info("Saving new passenger with email {} and phone number {}",
                passengerTO.getEmail(), passengerTO.getPhoneNumber());
        checkDuplications(passengerTO);
        Passenger passengerToSave = mapper.requestToPassenger(passengerTO);
        String email = getUserEmail();
        if ((passengerToSave.getEmail().equals(email) && !isAdmin()) || isAdmin()) {
            BigDecimal defaultRating = BigDecimal.valueOf(5);
            defaultRating = defaultRating.setScale(2, RoundingMode.HALF_UP);
            passengerToSave.setRating(defaultRating);
            passengerToSave.setRatingUpdateTimestamp(LocalDateTime.now());
            Passenger savedPassenger = repository.save(passengerToSave);
            log.info("New passenger with id {} saved successfully", savedPassenger.getId());
            return mapper.passengerToResponse(savedPassenger);
        } else {
            log.warn("User and entity email during passenger creation are different: user email: {}, passenger email {}", email, passengerTO.getEmail());
            throw new InvalidCredentialsException("Passenger email must be equal to email of user");
        }
    }

    @Transactional
    public void softDeletePassenger(Long id) {
        log.info("Softly deleting passenger with id {}", id);
        Optional<Passenger> passenger = repository.findById(id);
        if (passenger.isEmpty()) {
            log.warn("Passenger for soft delete with provided id {} not found", id);
            throw new PassengerNotFoundException();
        }
        String email = getUserEmail();
        if ((passenger.get().getEmail().equals(email) && !isAdmin()) || isAdmin()) {
            passenger.get().setRemoveStatus(RemoveStatus.REMOVED);
            updatePassenger(mapper.passengerToRequest(passenger.get()));
            log.info("Soft delete complete for passenger with id {}", id);
        } else {
            log.warn("User and entity email during passenger delete are different: user email: {}, passenger email {}",
                    email, passenger.get().getEmail());
            throw new InvalidCredentialsException("Delete of user data allowed for same account only");
        }
    }

    @Transactional
    public void deletePassenger(Long id) {
        log.info("Deleting passenger with id {}", id);
        repository.delete(repository.findById(id).orElseThrow(PassengerNotFoundException::new));
        log.info("Passenger with id {} deleted successfully", id);
    }

    private void checkDuplications(PassengerRequestTO passengerTO) {
        repository.getByEmail(passengerTO.getEmail()).ifPresent((passenger -> {
            if (!Objects.equals(passenger.getId(), passengerTO.getId())) {
                log.warn("Email \"{}\" is duplicated within passenger service", passengerTO.getEmail());
                throw new DuplicateEmailException();
            }
        }));
        repository.getByPhoneNumber(passengerTO.getPhoneNumber()).ifPresent((passenger -> {
            if (!Objects.equals(passenger.getId(), passengerTO.getId())) {
                log.warn("Phone number \"{}\" is duplicated within passenger service", passengerTO.getPhoneNumber());
                throw new DuplicatePhoneNumberException();
            }
        }));
    }

    private RatingEvaluationResponseTO evaluateMeanRating(PassengerRequestTO passengerTO) {
        return ratingClient.evaluateRating("PASSENGER", passengerTO.getId());
    }

    private Passenger updatePassengerDriverRating(Passenger passenger) {
        log.info("Evaluating mean rating for passenger with id {} during update", passenger.getId());
        RatingEvaluationResponseTO evaluatedRating = evaluateMeanRating(mapper.passengerToRequest(passenger));
        passenger.setRating(evaluatedRating.getMeanEvaluation());
        passenger.setRatingUpdateTimestamp(LocalDateTime.now());
        Passenger updatedPassenger = repository.save(passenger);
        log.info("Mean rating for passenger with id {} is up to date", passenger.getId());
        return updatedPassenger;
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
