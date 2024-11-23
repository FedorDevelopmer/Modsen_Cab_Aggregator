package com.modsen.software.passenger.service.impl;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.dto.RatingEvaluationResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.exception.BadEvaluationRequestException;
import com.modsen.software.passenger.exception.DuplicateEmailException;
import com.modsen.software.passenger.exception.DuplicatePhoneNumberException;
import com.modsen.software.passenger.exception.PassengerNotFoundException;
import com.modsen.software.passenger.filter.PassengerFilter;
import com.modsen.software.passenger.mapper.PassengerMapper;
import com.modsen.software.passenger.repository.PassengerRepository;
import com.modsen.software.passenger.service.PassengerService;
import com.modsen.software.passenger.specification.PassengerSpecification;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class PassengerServiceImpl implements PassengerService {
    @Autowired
    private PassengerRepository repository;

    @Autowired
    private PassengerMapper mapper;

    WebClient ratingClient = WebClient.builder()
            .baseUrl("http://localhost:8083/api/v1/scores")
            .build();

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
        log.info("Fetching passenger by id {}",id);
        Optional<Passenger> passengerOptional = repository.findById(id);
        if (passengerOptional.isEmpty()) {
            log.warn("Passenger with provided id {} not found",id);
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
        log.info("Updating passenger with id {}",passengerTO.getId());
        Optional<Passenger> oldPassengerOptional = repository.findById(passengerTO.getId());
        if(oldPassengerOptional.isEmpty()){
            log.warn("Passenger for update with provided id {} not found",passengerTO.getId());
            throw new PassengerNotFoundException();
        }
        checkDuplications(passengerTO);
        Passenger oldPassenger = oldPassengerOptional.get();
        Passenger passengerToUpdate = mapper.requestToPassenger(passengerTO);
        passengerToUpdate.setRatingUpdateTimestamp(oldPassenger.getRatingUpdateTimestamp());
        passengerToUpdate.setRating(oldPassenger.getRating());
        if (!LocalDateTime.now().isBefore(oldPassenger.getRatingUpdateTimestamp().plusDays(1))) {
            Passenger updatedPassenger = updatePassengerDriverRating(passengerToUpdate);
            log.info("Update for passenger with id {} complete successfully(mean rating updated)",passengerTO.getId());
            return mapper.passengerToResponse(updatedPassenger);
        } else {
            Passenger updatedPassenger = repository.save(passengerToUpdate);
            log.info("Update for passenger with id {} complete successfully(mean rating not updated)",passengerTO.getId());
            return mapper.passengerToResponse(updatedPassenger);
        }
    }

    @Transactional
    public void updatePassengerByKafka(RatingEvaluationResponseTO ratingEvaluation) {
        log.info("Updating rating of passenger with id {} through Kafka",ratingEvaluation.getId());
        Passenger passenger = repository.findById(ratingEvaluation.getId()).orElseThrow(PassengerNotFoundException::new);
        passenger.setRating(ratingEvaluation.getMeanEvaluation());
        passenger.setRatingUpdateTimestamp(LocalDateTime.now());
        repository.save(passenger);
        log.info("Mean rating for passenger with id {} is updated through Kafka",ratingEvaluation.getId());
    }

    @Transactional
    public PassengerResponseTO savePassenger(PassengerRequestTO passengerTO) {
        log.info("Saving new passenger with email {} and phone number {}",
                passengerTO.getEmail(), passengerTO.getPhoneNumber());
        checkDuplications(passengerTO);
        Passenger passengerToSave = mapper.requestToPassenger(passengerTO);
        BigDecimal defaultRating = BigDecimal.valueOf(5);
        defaultRating = defaultRating.setScale(2, RoundingMode.HALF_UP);
        passengerToSave.setRating(defaultRating);
        passengerToSave.setRatingUpdateTimestamp(LocalDateTime.now());
        Passenger savedPassenger = repository.save(passengerToSave);
        log.info("New passenger with id {} saved successfully",savedPassenger.getId());
        return mapper.passengerToResponse(savedPassenger);
    }

    @Transactional
    public void softDeletePassenger(Long id) {
        log.info("Softly deleting passenger with id {}", id);
        Optional<Passenger> passenger = repository.findById(id);
        if(passenger.isEmpty()){
            log.warn("Passenger for soft delete with provided id {} not found",id);
        }
        passenger.orElseThrow(PassengerNotFoundException::new).setRemoveStatus(RemoveStatus.REMOVED);
        updatePassenger(mapper.passengerToRequest(passenger.get()));
        log.info("Soft delete complete for passenger with id {}", id);
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
        return ratingClient.get()
                .uri("/evaluate/{id}?initiator=PASSENGER", passengerTO.getId())
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatusCode.valueOf(404)), response -> {
                    log.warn("Passenger with id {} not found during rating evaluation", passengerTO.getId());
                    throw new PassengerNotFoundException();
                })
                .onStatus(status -> status.isSameCodeAs(HttpStatusCode.valueOf(400)), response -> {
                    log.warn("Bad request for rating evaluation of passenger with id {}", passengerTO.getId());
                    throw new BadEvaluationRequestException();
                })
                .bodyToMono(RatingEvaluationResponseTO.class)
                .block();
    }

    private Passenger updatePassengerDriverRating(Passenger passenger) {
        log.info("Evaluating mean rating for passenger with id {} during update",passenger.getId());
        RatingEvaluationResponseTO evaluatedRating = evaluateMeanRating(mapper.passengerToRequest(passenger));
        passenger.setRating(evaluatedRating.getMeanEvaluation());
        passenger.setRatingUpdateTimestamp(LocalDateTime.now());
        Passenger updatedPassenger = repository.save(passenger);
        log.info("Mean rating for passenger with id {} is up to date",passenger.getId());
        return updatedPassenger;
    }
}
