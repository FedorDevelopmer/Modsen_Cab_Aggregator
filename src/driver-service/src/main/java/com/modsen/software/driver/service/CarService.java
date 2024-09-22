package com.modsen.software.driver.service;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.filter.CarFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarService {

    Page<CarResponseTO> getAllCars(CarFilter filter, Pageable pageable);

    CarResponseTO findCarById(Long id);

    CarResponseTO saveCar(CarRequestTO carRequest);

    CarResponseTO updateCar(CarRequestTO carRequest);

    void deleteCar(Long id);
}
