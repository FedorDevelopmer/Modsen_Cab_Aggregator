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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class CarServiceImpl implements CarService {
    @Autowired
    private CarRepository repository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarMapper mapper;

    @Transactional
    public Page<CarResponseTO> getAllCars(CarFilter filter, Pageable pageable) {
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
        Optional<Car> car = repository.findById(id);
        return mapper.carToResponse(car.orElseThrow(CarNotFoundException::new));
    }

    @Transactional
    public CarResponseTO updateCar(CarRequestTO carTO) {
        checkDuplications(carTO);
        repository.findById(carTO.getId()).orElseThrow(CarNotFoundException::new);
        Driver updatedCarDriver = driverRepository.findById(carTO.getDriverId()).orElseThrow(DriverNotFoundException::new);
        Car updatedCar = repository.save(mapper.requestToCar(carTO));
        updatedCarDriver.getCars().add(updatedCar);
        updatedCar.setDriver(updatedCarDriver);
        return mapper.carToResponse(updatedCar);
    }

    @Transactional
    public CarResponseTO saveCar(CarRequestTO carTO) {
        Driver carDriver = driverRepository.findById(carTO.getDriverId()).orElseThrow(DriverNotFoundException::new);
        checkDuplications(carTO);
        Car savedCar = repository.save(mapper.requestToCar(carTO));
        carDriver.getCars().add(savedCar);
        savedCar.setDriver(carDriver);
        driverRepository.save(carDriver);
        return mapper.carToResponse(savedCar);
    }

    @Transactional
    public void softDeleteCar(Long id) {
        Optional<Car> car = repository.findById(id);
        car.orElseThrow(CarNotFoundException::new).setRemoveStatus(RemoveStatus.REMOVED);
        updateCar(mapper.carToRequest(car.get()));
    }

    @Transactional
    public void deleteCar(Long id) {
        repository.delete(mapper.responseToCar(findCarById(id)));
    }

    private void checkDuplications(CarRequestTO carTO) {
        repository.findByRegistrationNumber(carTO.getRegistrationNumber()).ifPresent((car -> {
            if (!Objects.equals(car.getId(), carTO.getId())) {
                throw new DuplicateRegistrationNumberException();
            }
        }));
    }
}
