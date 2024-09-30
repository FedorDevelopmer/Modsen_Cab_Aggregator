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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

@Service
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
        Specification<Passenger> spec = Specification.where(PassengerSpecification.hasEmail(filter.getEmail()))
                .and(PassengerSpecification.hasName(filter.getName()))
                .and(PassengerSpecification.hasGender(filter.getGender()))
                .and(PassengerSpecification.hasPhone(filter.getPhoneNumber()))
                .and(PassengerSpecification.hasRemoveStatus(filter.getRemoveStatus()));
        return repository.findAll(spec, pageable).map((item) -> mapper.passengerToResponse(item));
    }

    @Transactional
    public PassengerResponseTO findPassengerById(Long id) {
        Optional<Passenger> passenger = repository.findById(id);
        return mapper.passengerToResponse(passenger.orElseThrow(PassengerNotFoundException::new));
    }

    @Transactional
    public PassengerResponseTO updatePassenger(PassengerRequestTO passengerTO) {
        repository.findById(passengerTO.getId()).orElseThrow(PassengerNotFoundException::new);
        checkDuplications(passengerTO);
        RatingEvaluationResponseTO evaluatedRatingResponse = evaluateMeanRating(passengerTO);
        Passenger passengerToUpdate = mapper.requestToPassenger(passengerTO);
        passengerToUpdate.setRating(evaluatedRatingResponse.getMeanEvaluation());
        return mapper.passengerToResponse(repository.save(passengerToUpdate));
    }

    @Transactional
    public PassengerResponseTO savePassenger(PassengerRequestTO passengerTO) {
        checkDuplications(passengerTO);
        Passenger passengerToSave = mapper.requestToPassenger(passengerTO);
        passengerToSave.setRating(BigDecimal.valueOf(5));
        return mapper.passengerToResponse(repository.save(passengerToSave));
    }

    @Transactional
    public void softDeletePassenger(Long id) {
        Optional<Passenger> passenger = repository.findById(id);
        passenger.orElseThrow(PassengerNotFoundException::new).setRemoveStatus(RemoveStatus.REMOVED);
        updatePassenger(mapper.passengerToRequest(passenger.get()));
    }

    @Transactional
    public void deletePassenger(Long id) {
        repository.delete(repository.findById(id).orElseThrow(PassengerNotFoundException::new));
    }

    private RatingEvaluationResponseTO evaluateMeanRating(PassengerRequestTO passengerTO) {
        return ratingClient.get()
                .uri("/evaluate/{id}?initiator=PASSENGER", passengerTO.getId())
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatusCode.valueOf(404)), response -> {
                    throw new PassengerNotFoundException();
                })
                .onStatus(status -> status.isSameCodeAs(HttpStatusCode.valueOf(400)), response -> {
                    throw new BadEvaluationRequestException();
                })
                .bodyToMono(RatingEvaluationResponseTO.class)
                .block();
    }

    private void checkDuplications(PassengerRequestTO passengerTO) {
        repository.getByEmail(passengerTO.getEmail()).ifPresent((passenger -> {
            if (!Objects.equals(passenger.getId(), passengerTO.getId())) {
                throw new DuplicateEmailException();
            }
        }));
        repository.getByPhoneNumber(passengerTO.getPhoneNumber()).ifPresent((passenger -> {
            if (!Objects.equals(passenger.getId(), passengerTO.getId())) {
                throw new DuplicatePhoneNumberException();
            }
        }));
    }
}
