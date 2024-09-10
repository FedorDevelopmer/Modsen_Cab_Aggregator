package com.modsen.software.passenger.service.impl;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.exception.DuplicateEmailException;
import com.modsen.software.passenger.exception.DuplicatePhoneNumberException;
import com.modsen.software.passenger.exception.PassengerNotFoundException;
import com.modsen.software.passenger.mapper.PassengerMapper;
import com.modsen.software.passenger.repository.PassengerRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PassengerServiceImpl {
    @Autowired
    private PassengerRepository repository;

    @Autowired
    private PassengerMapper mapper;

    @Transactional
    public List<PassengerResponseTO> getAllPassengers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return repository.findAll(PageRequest.of(pageNumber, pageSize,
                         Sort.by(Sort.Direction.valueOf(sortOrder), sortBy)))
                         .stream()
                         .map(mapper::passengerToResponse)
                         .collect(Collectors.toList());
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
        return mapper.passengerToResponse(repository.save(mapper.requestToPassenger(passengerTO)));
    }

    @Transactional
    public PassengerResponseTO savePassenger(PassengerRequestTO passengerTO) {
        checkDuplications(passengerTO);
        return mapper.passengerToResponse(repository.save(mapper.requestToPassenger(passengerTO)));
    }

    @Transactional
    public void softDeletePassenger(Long id) {
        Optional<Passenger> passenger = repository.findById(id);
        passenger.orElseThrow(PassengerNotFoundException::new).setRemoveStatus(RemoveStatus.REMOVED);
        updatePassenger(mapper.passengerToRequest(passenger.get()));
    }

    @Transactional
    public void deletePassenger(Long id) {
        repository.delete(mapper.responseToPassenger(findPassengerById(id)));
    }

    private void checkDuplications(PassengerRequestTO passengerTO){
        repository.getByEmail(passengerTO.getEmail()).ifPresent((passenger -> {throw new DuplicateEmailException();}));
        repository.getByPhoneNumber(passengerTO.getPhoneNumber()).ifPresent((passenger -> {throw new DuplicatePhoneNumberException();}));
    }
}
