package com.modsen.software.driver.service.impl;

import com.modsen.software.driver.dto.*;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateEmailException;
import com.modsen.software.driver.exception.DuplicatePhoneException;
import com.modsen.software.driver.filter.DriverFilter;
import com.modsen.software.driver.mapper.CarMapper;
import com.modsen.software.driver.mapper.DriverMapper;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.DriverService;
import com.modsen.software.driver.specification.DriverSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepository repository;

    @Autowired
    private CarRepository carsRepository;

    @Autowired
    private DriverMapper mapper;

    @Autowired
    private CarMapper carMapper;

    WebClient ratingClient = WebClient.builder()
            .baseUrl("http://localhost:8083/api/v1/scores")
            .build();

    @Transactional
    public Page<DriverResponseTO> getAllDrivers(DriverFilter filter, Pageable pageable) {
        Specification<Driver> spec = Specification.where(DriverSpecification.hasName(filter.getName()))
                .and(DriverSpecification.hasSurname(filter.getSurname()))
                .and(DriverSpecification.hasEmail(filter.getEmail()))
                .and(DriverSpecification.hasPhone(filter.getPhoneNumber()))
                .and(DriverSpecification.hasGender(filter.getGender()))
                .and(DriverSpecification.hasBirthDateEarlier(filter.getBirthDateEarlier()))
                .and(DriverSpecification.hasBirthDate(filter.getBirthDate()))
                .and(DriverSpecification.hasBirthDateLater(filter.getBirthDateLater()))
                .and(DriverSpecification.hasRemoveStatus(filter.getRemoveStatus()));
        return repository.findAll(spec, pageable).map((item)-> mapper.driverToResponse(item));

    }

    @Transactional
    public DriverResponseTO findDriverById(Long id) {
        Optional<Driver> driver = repository.findById(id);
        return mapper.driverToResponse(driver.orElseThrow(DriverNotFoundException::new));
    }

    @Transactional
    public DriverResponseTO updateDriver(DriverRequestTO driverTO) {
        repository.findById(driverTO.getId()).orElseThrow(DriverNotFoundException::new);
        checkDuplications(driverTO);
        return mapper.driverToResponse(repository.save(mapper.requestToDriver(driverTO)));
    }

    @Transactional
    public DriverResponseTO saveDriver(DriverRequestTO driverTO) {
        checkDuplications(driverTO);
        if(Objects.nonNull(driverTO.getCars())) {
            Set<DriverRelatedCarRequestTO> requestCars = Set.copyOf(driverTO.getCars());
            driverTO.getCars().clear();
            Driver savedDriver = repository.save(mapper.requestToDriver(driverTO));
            for (DriverRelatedCarRequestTO relatedCarRequestTO : requestCars) {
                Car carToSave = carMapper.driverRelatedRequestToCar(relatedCarRequestTO);
                carToSave.setDriverId(savedDriver.getId());
                carToSave.setDriver(savedDriver);
                savedDriver.getCars().add(carsRepository.save(carToSave));
            }
            return mapper.driverToResponse(savedDriver);
        } else {
            driverTO.setCars(new HashSet<>());
            return mapper.driverToResponse(repository.save(mapper.requestToDriver(driverTO)));
        }

    }

    @Transactional
    public void softDeleteDriver(Long id) {
        Optional<Driver> driver = repository.findById(id);
        driver.orElseThrow(DriverNotFoundException::new).setRemoveStatus(RemoveStatus.REMOVED);
        updateDriver(mapper.driverToRequest(driver.get()));
    }

    @Transactional
    public void deleteDriver(Long id) {
        repository.delete(mapper.responseToDriver(findDriverById(id)));
    }

    private void checkDuplications(DriverRequestTO driverTO) {
        repository.findByEmail(driverTO.getEmail()).ifPresent(driver -> {
            if(!Objects.equals(driver.getId(),driverTO.getId())) {
                throw new DuplicateEmailException();
            }
        });
        repository.findByPhoneNumber(driverTO.getPhoneNumber()).ifPresent(driver -> {
            if(!Objects.equals(driver.getId(),driverTO.getId())) {
                throw new DuplicatePhoneException();
            }
        });
    }
}
