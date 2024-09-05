package com.modsen.software.driver.mapper;

import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.entity.Driver;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DriverMapper {
    DriverResponseTO driverToResponse(Driver driver);
    Driver responseToDriver(DriverResponseTO driverResponseTo);
    DriverRequestTO driverToRequest(Driver driver);
    Driver requestToDriver(DriverRequestTO driverRequestTo);
}
