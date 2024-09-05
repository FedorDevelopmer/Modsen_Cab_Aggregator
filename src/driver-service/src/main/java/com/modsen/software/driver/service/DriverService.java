package com.modsen.software.driver.service;

import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;

import java.util.List;

public interface DriverService {

    List<DriverResponseTO> getAllDrivers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);

    DriverResponseTO findDriverById(Long id);

    DriverResponseTO saveDriver(DriverRequestTO carRequest);

    DriverResponseTO updateDriver(DriverRequestTO carRequest);

    void deleteDriver(Long id);

}
