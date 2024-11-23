package com.modsen.software.driver.service.impl;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.CarNotFoundException;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateRegistrationNumberException;
import com.modsen.software.driver.filter.CarFilter;
import com.modsen.software.driver.mapper.CarMapper;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.CarService;
import com.modsen.software.driver.specification.CarSpecification;
import jakarta.transaction.Transactional;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CarServiceImpl implements CarService {
    @Autowired
    private CarRepository repository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarMapper mapper;

    @Transactional
    public Page<CarResponseTO> getAllCars(CarFilter filter, Pageable pageable) {
        log.info("Fetching cars page - page number: {},page size: {}, is first: {}",
                pageable.getPageNumber(), pageable.getPageSize(), !pageable.hasPrevious());
        Specification<Car> spec = Specification.where(CarSpecification.hasBrand(filter.getBrand()))
                .and(CarSpecification.hasColor(filter.getColor()))
                .and(CarSpecification.hasRegistrationNumber(filter.getRegistrationNumber()))
                .and(CarSpecification.hasInspectionDateEarlier(filter.getInspectionDateEarlier()))
                .and(CarSpecification.hasInspectionDate(filter.getInspectionDate()))
                .and(CarSpecification.hasInspectionDateLater(filter.getInspectionDateLater()))
                .and(CarSpecification.hasInspectionDurationMonth(filter.getInspectionDurationMonth())
                        .and(CarSpecification.hasRemoveStatus(filter.getRemoveStatus())));
        return repository.findAll(spec, pageable).map((item) -> mapper.carToResponse(item));
    }

    @Transactional
    public CarResponseTO findCarById(Long id) {
        log.info("Fetching car by id {}",id);
        Optional<Car> car = repository.findById(id);
        if(car.isEmpty()){
            log.warn("Car with provided id {} not found",id);
        }
        return mapper.carToResponse(car.orElseThrow(CarNotFoundException::new));
    }

    @Transactional
    public CarResponseTO updateCar(CarRequestTO carTO) {
        log.info("Updating car with id {}", carTO.getId());
        checkDuplications(carTO);
        if(repository.findById(carTO.getId()).isEmpty()){
            log.warn("Car for update with provided id {} not found",carTO.getId());
            throw new CarNotFoundException();
        }
        Optional<Driver> updatedCarDriver = driverRepository.findById(carTO.getDriverId());
        if(updatedCarDriver.isEmpty()){
            log.warn("Driver with id {} for updating car with id {} not found",carTO.getDriverId(), carTO.getId());
        }
        Car updatedCar = repository.save(mapper.requestToCar(carTO));
        log.info("Update for car with id {} complete successfully",carTO.getId());
        updatedCarDriver.orElseThrow(DriverNotFoundException::new).getCars().add(updatedCar);
        updatedCar.setDriver(updatedCarDriver.get());
        return mapper.carToResponse(updatedCar);
    }

    @Transactional
    public CarResponseTO saveCar(CarRequestTO carTO) {
        log.info("Saving new car with registration number {}", carTO.getRegistrationNumber());
        Optional<Driver> carDriver = driverRepository.findById(carTO.getDriverId());
        if(carDriver.isEmpty()){
            log.warn("Driver with id {} for creating new car not found",carTO.getId());
        }
        checkDuplications(carTO);
        Car savedCar = repository.save(mapper.requestToCar(carTO));
        log.info("New car with id {} saved successfully", savedCar.getId());
        carDriver.orElseThrow(DriverNotFoundException::new).getCars().add(savedCar);
        savedCar.setDriver(carDriver.get());
        driverRepository.save(carDriver.get());
        log.info("New car saved in car list of driver with id {}", carDriver.get().getId());
        return mapper.carToResponse(savedCar);
    }

    @Transactional
    public void softDeleteCar(Long id) {
        log.info("Softly deleting car with id {}", id);
        Optional<Car> car = repository.findById(id);
        if(car.isEmpty()){
            log.warn("Car for soft delete with provided id {} not found",id);
        }
        car.orElseThrow(CarNotFoundException::new).setRemoveStatus(RemoveStatus.REMOVED);
        updateCar(mapper.carToRequest(car.get()));
        log.info("Soft delete complete for car with id {}", id);
    }

    @Transactional
    public void deleteCar(Long id) {
        log.info("Deleting car with id {}", id);
        repository.delete(mapper.responseToCar(findCarById(id)));
        log.info("Car with id {} deleted successfully", id);
    }

    private void checkDuplications(CarRequestTO carTO) {
        repository.findByRegistrationNumber(carTO.getRegistrationNumber()).ifPresent((car -> {
            if (!Objects.equals(car.getId(), carTO.getId())) {
                log.warn("Car registration number \"{}\" is duplicated within driver service", carTO.getRegistrationNumber());
                throw new DuplicateRegistrationNumberException();
            }
        }));
    }
}
