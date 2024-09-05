package com.modsen.software.driver.service;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;

import java.util.List;

public interface CarService {

    List<CarResponseTO> getAllCars(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    CarResponseTO findCarById(Long id);

    CarResponseTO saveCar(CarRequestTO carRequest);

    CarResponseTO updateCar(CarRequestTO carRequest);

    void deleteCar(Long id);

}
