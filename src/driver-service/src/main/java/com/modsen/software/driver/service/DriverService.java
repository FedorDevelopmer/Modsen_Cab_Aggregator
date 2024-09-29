package com.modsen.software.driver.service;

import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.dto.RatingEvaluationResponseTO;
import com.modsen.software.driver.filter.DriverFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface DriverService {

    Page<DriverResponseTO> getAllDrivers(DriverFilter filter, Pageable pageable);

    DriverResponseTO findDriverById(Long id);

    DriverResponseTO saveDriver(DriverRequestTO driverRequest);

    DriverResponseTO updateDriver(DriverRequestTO driverRequest);

    void updateDriverByKafka(RatingEvaluationResponseTO ratingEvaluationResponseTO);

    void softDeleteDriver(Long id);

    void deleteDriver(Long id);
}
