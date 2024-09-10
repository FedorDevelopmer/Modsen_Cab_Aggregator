package com.modsen.software.driver.service.impl;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.CarNotFoundException;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateRegistrationNumberException;
import com.modsen.software.driver.mapper.CarMapper;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.CarService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarServiceImpl implements CarService {
    @Autowired
    private CarRepository repository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarMapper mapper;

    @Transactional
    public List<CarResponseTO> getAllCars(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return repository.findAll(PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Direction.valueOf(sortOrder), sortBy)))
                        .stream()
                        .map(mapper::carToResponse)
                        .collect(Collectors.toList());
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
        driverRepository.findById(carTO.getDriverId()).orElseThrow(DriverNotFoundException::new);
        return saveCar(carTO);
    }

    @Transactional
    public CarResponseTO saveCar(CarRequestTO carTO) {
        driverRepository.findById(carTO.getDriverId()).orElseThrow(DriverNotFoundException::new);
        checkDuplications(carTO);
        carTO.setId(null);
        Car saved = repository.save(mapper.requestToCar(carTO));
        return mapper.carToResponse(saved);
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
            throw new DuplicateRegistrationNumberException();
        }));
    }
}
