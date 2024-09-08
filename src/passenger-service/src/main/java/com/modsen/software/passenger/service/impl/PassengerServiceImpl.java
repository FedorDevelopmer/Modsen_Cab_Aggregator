package com.modsen.software.passenger.service.impl;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.exception.DuplicateEmailException;
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

@Service
public class PassengerServiceImpl {
    @Autowired
    private PassengerRepository repository;

    @Autowired
    private PassengerMapper mapper;

    @Transactional
    public List<PassengerResponseTO> getAllPassengers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        List<PassengerResponseTO> passengersList = new ArrayList<>();
        Iterable<Passenger> passengers = repository.findAll(PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.valueOf(sortOrder), sortBy)));
        for (Passenger p : passengers) {
            passengersList.add(mapper.passengerToResponse(p));
        }
        return passengersList;
    }

    @Transactional
    public PassengerResponseTO findPassengerById(Long id) {
        Optional<Passenger> passenger = repository.findById(id);
        if (passenger.isPresent()) {
            return mapper.passengerToResponse(passenger.get());
        } else {
            throw new PassengerNotFoundException("Passenger with id " + id + " doesn't exist");
        }
    }

    @Transactional
    public PassengerResponseTO updatePassenger(PassengerRequestTO passengerTO) {
        if (passengerTO.getId() == null) {
            throw new RuntimeException("Passenger id for update must not be 'null'");
        }
        if (repository.findById(passengerTO.getId()).isEmpty()) {
            throw new PassengerNotFoundException("Passenger with id " + passengerTO.getId() + " doesn't exist");
        }
        Optional<Passenger> passengerWithSameEmail = repository.getByEmail(passengerTO.getEmail());
        Optional<Passenger> passengerWithSamePhone = repository.getByPhoneNumber(passengerTO.getPhoneNumber());
        if (passengerWithSameEmail.isPresent() && passengerWithSameEmail.get().getEmail().equals(passengerTO.getEmail())) {
            throw new DuplicateEmailException("Passenger with email " + passengerTO.getEmail() + " already exist. Use another email.");
        }
        if(passengerWithSamePhone.isPresent() && passengerWithSamePhone.get().getPhoneNumber().equals(passengerTO.getPhoneNumber())) {
            throw new DuplicateEmailException("Passenger with phone number " + passengerTO.getPhoneNumber() + " already exist. Use another phone number.");
        }

        return savePassenger(passengerTO);
    }

    @Transactional
    public PassengerResponseTO savePassenger(PassengerRequestTO passengerTO) {
        Optional<Passenger> passengerWithSameEmail = repository.getByEmail(passengerTO.getEmail());
        Optional<Passenger> passengerWithSamePhone = repository.getByPhoneNumber(passengerTO.getPhoneNumber());
        if (passengerWithSameEmail.isPresent() && passengerWithSameEmail.get().getEmail().equals(passengerTO.getEmail())) {
            throw new DuplicateEmailException("Passenger with email " + passengerTO.getEmail() + " already exist. Use another email.");
        }
        if(passengerWithSamePhone.isPresent() && passengerWithSamePhone.get().getPhoneNumber().equals(passengerTO.getPhoneNumber())) {
            throw new DuplicateEmailException("Passenger with phone number " + passengerTO.getPhoneNumber() + " already exist. Use another phone number.");
        }

        Passenger saved = repository.save(mapper.requestToPassenger(passengerTO));
        return mapper.passengerToResponse(saved);
    }

    @Transactional
    public boolean softDeletePassenger(Long id) {
        Optional<Passenger> passenger = repository.findById(id);
        if (passenger.isPresent()) {
            if (passenger.get().getRemoveStatus().equals(RemoveStatus.REMOVED)) {
                return false;
            }
            passenger.get().setRemoveStatus(RemoveStatus.REMOVED);
            updatePassenger(mapper.passengerToRequest(passenger.get()));
            return true;
        } else {
            throw new PassengerNotFoundException("Passenger with id " + id + " doesn't exist");
        }
    }

    @Transactional
    public void deletePassenger(Long id) {
        repository.delete(mapper.responseToPassenger(findPassengerById(id)));
    }
}
