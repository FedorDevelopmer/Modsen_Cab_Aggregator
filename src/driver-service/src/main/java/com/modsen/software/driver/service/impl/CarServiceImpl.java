package com.modsen.software.driver.service.impl;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.CarNotFoundException;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.mapper.CarMapper;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.CarService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CarServiceImpl implements CarService {
    @Autowired
    private CarRepository repository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private CarMapper mapper;

    @Transactional
    public List<CarResponseTO> getAllCars(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder){
        List<CarResponseTO> carsList = new ArrayList<>();
        Iterable<Car> cars = repository.findAll(PageRequest.of(pageNumber,pageSize, Sort.by(Sort.Direction.valueOf(sortOrder), sortBy)));
        for(Car car : cars){
            carsList.add(mapper.carToResponse(car));
        }
        return carsList;
    }

    @Transactional
    public CarResponseTO findCarById(Long id){
        Optional<Car> car = repository.findById(id);
        if(car.isPresent()){
            return mapper.carToResponse(car.get());
        } else {
            throw new CarNotFoundException("Car with id " + id + " doesn't exist");
        }
    }

    @Transactional
    public CarResponseTO updateCar(CarRequestTO carTO){
        if(repository.findById(carTO.getId()).isEmpty()){
            throw new CarNotFoundException("Car with id " + carTO.getId() + " doesn't exist");
        }
        return saveCar(carTO);
    }

    @Transactional
    public CarResponseTO saveCar(CarRequestTO carTO){
        if(driverRepository.findById(carTO.getDriverId()).isEmpty()){
            throw new DriverNotFoundException("Driver with id " + carTO.getId() + " doesn't exist. Unable to create car with non-existing driver");
        }
        Car saved = repository.save(mapper.requestToCar(carTO));
        return mapper.carToResponse(saved);
    }

    @Transactional
    public void softDeleteCar(Long id){
        Optional<Car> car = repository.findById(id);
        if(car.isPresent()){
            car.get().setRemoveStatus(RemoveStatus.REMOVED);
            updateCar(mapper.carToRequest(car.get()));
        } else {
            throw new CarNotFoundException("Car with id " + id + " doesn't exist");
        }
    }

    @Transactional
    public void deleteCar(Long id){
        repository.delete(mapper.responseToCar(findCarById(id)));
    }
}
