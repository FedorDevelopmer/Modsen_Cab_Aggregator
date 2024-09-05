package com.modsen.software.driver.mapper;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.entity.Car;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CarMapper {
    CarResponseTO carToResponse(Car car);

    Car responseToCar(CarResponseTO carResponseTo);

    CarRequestTO carToRequest(Car car);

    Car requestToCar(CarRequestTO carRequestTo);
}
