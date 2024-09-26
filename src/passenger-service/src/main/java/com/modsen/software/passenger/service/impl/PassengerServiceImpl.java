package com.modsen.software.passenger.service.impl;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
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
import org.springframework.stereotype.Service;
import java.util.Objects;
import java.util.Optional;

@Service
public class PassengerServiceImpl implements PassengerService {
    @Autowired
    private PassengerRepository repository;

    @Autowired
    private PassengerMapper mapper;

    @Transactional
    public Page<PassengerResponseTO> getAllPassengers(PassengerFilter filter, Pageable pageable) {
        Specification<Passenger> spec = Specification.where(PassengerSpecification.hasEmail(filter.getEmail()))
                .and(PassengerSpecification.hasName(filter.getName()))
                .and(PassengerSpecification.hasGender(filter.getGender()))
                .and(PassengerSpecification.hasPhone(filter.getPhoneNumber()))
                .and(PassengerSpecification.hasRemoveStatus(filter.getRemoveStatus()));
        return repository.findAll(spec,pageable).map((item)-> mapper.passengerToResponse(item));
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
        repository.delete(repository.findById(id).orElseThrow(PassengerNotFoundException::new));
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
