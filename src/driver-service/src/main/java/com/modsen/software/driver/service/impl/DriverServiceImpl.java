package com.modsen.software.driver.service.impl;

import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.mapper.DriverMapper;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.repository.DriverRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DriverServiceImpl {
    @Autowired
    private DriverRepository repository;

    @Autowired
    private DriverMapper mapper;

    @Transactional
    public List<DriverResponseTO> getAllDrivers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder){
        List<DriverResponseTO> driversList = new ArrayList<>();
        Iterable<Driver> drivers = repository.findAll(PageRequest.of(pageNumber,pageSize, Sort.by(Sort.Direction.valueOf(sortOrder), sortBy)));
        for(Driver d : drivers){
           driversList.add(mapper.driverToResponse(d));
        }
        return driversList;
    }

    @Transactional
    public DriverResponseTO findDriverById(Long id){
        Optional<Driver> driver = repository.findById(id);
        if(driver.isPresent()){
            return mapper.driverToResponse(driver.get());
        } else {
            throw new DriverNotFoundException("Driver with id " + id + " doesn't exist");
        }
    }

    @Transactional
    public DriverResponseTO updateDriver(DriverRequestTO driverTO){
        if(repository.findById(driverTO.getId()).isEmpty()){
            throw new DriverNotFoundException("Driver with id " +driverTO.getId() + " doesn't exist");
        }
        return saveDriver(driverTO);
    }

    @Transactional
    public DriverResponseTO saveDriver(DriverRequestTO driverTO){
        Driver saved = repository.save(mapper.requestToDriver(driverTO));
        return mapper.driverToResponse(saved);
    }

    @Transactional
    public void softDeleteDriver(Long id){
        Optional<Driver>driver = repository.findById(id);
        if(driver.isPresent()){
           driver.get().setRemoveStatus(RemoveStatus.REMOVED);
            updateDriver(mapper.driverToRequest(driver.get()));
        } else {
            throw new DriverNotFoundException("Driver with id " + id + " doesn't exist");
        }
    }

    @Transactional
    public void deleteDriver(Long id){
        repository.delete(mapper.responseToDriver(findDriverById(id)));
    }
}
