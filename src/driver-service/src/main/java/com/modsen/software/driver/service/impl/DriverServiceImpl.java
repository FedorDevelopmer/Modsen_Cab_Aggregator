package com.modsen.software.driver.service.impl;

import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateEmailException;
import com.modsen.software.driver.exception.DuplicatePhoneException;
import com.modsen.software.driver.mapper.DriverMapper;
import com.modsen.software.driver.repository.DriverRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DriverServiceImpl {
    @Autowired
    private DriverRepository repository;

    @Autowired
    private DriverMapper mapper;

    @Transactional
    public List<DriverResponseTO> getAllDrivers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return repository.findAll(PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Direction.valueOf(sortOrder), sortBy)))
                .stream()
                .map(mapper::driverToResponse)
                .collect(Collectors.toList());

    }

    @Transactional
    public DriverResponseTO findDriverById(Long id) {
        Optional<Driver> driver = repository.findById(id);
        return mapper.driverToResponse(driver.orElseThrow(DriverNotFoundException::new));
    }

    @Transactional
    public DriverResponseTO updateDriver(DriverRequestTO driverTO) {
        repository.findById(driverTO.getId()).orElseThrow(DriverNotFoundException::new);
        checkDuplications(driverTO);
        return saveDriver(driverTO);
    }

    @Transactional
    public DriverResponseTO saveDriver(DriverRequestTO driverTO) {
        checkDuplications(driverTO);
        Driver saved = repository.save(mapper.requestToDriver(driverTO));
        return mapper.driverToResponse(saved);
    }

    @Transactional
    public void softDeleteDriver(Long id) {
        Optional<Driver> driver = repository.findById(id);
        driver.orElseThrow(DriverNotFoundException::new).setRemoveStatus(RemoveStatus.REMOVED);
        updateDriver(mapper.driverToRequest(driver.get()));
    }

    @Transactional
    public void deleteDriver(Long id) {
        repository.delete(mapper.responseToDriver(findDriverById(id)));
    }

    private void checkDuplications(DriverRequestTO driverTO) {
        repository.findByEmail(driverTO.getEmail()).ifPresent(driver -> {
            throw new DuplicateEmailException();
        });
        repository.findByPhoneNumber(driverTO.getPhoneNumber()).ifPresent(driver -> {
            throw new DuplicatePhoneException();
        });
    }
}
