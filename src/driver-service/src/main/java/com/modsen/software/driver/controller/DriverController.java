package com.modsen.software.driver.controller;

import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateEmailException;
import com.modsen.software.driver.exception.DuplicatePhoneException;
import com.modsen.software.driver.exception_handler.ExceptionHandling;
import com.modsen.software.driver.service.impl.DriverServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@Controller
@RequestMapping("/api/v1.0/driver")
public class DriverController {
    @Autowired
    private DriverServiceImpl service;

    @GetMapping
    public ResponseEntity<List<DriverResponseTO>> getAll(@RequestParam(required = false, defaultValue = "0") @Min(0) Integer pageNumber,
                                                         @RequestParam(required = false, defaultValue = "100") @Min(1) Integer pageSize,
                                                         @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                         @RequestParam(required = false, defaultValue = "ASC") String sortOrder) {
        List<DriverResponseTO> drivers = service.getAllDrivers(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseTO> findById(@PathVariable @Min(1) Long id) {
        DriverResponseTO driver = service.findDriverById(id);
        return new ResponseEntity<>(driver, HttpStatus.OK);

    }

    @PutMapping
    public ResponseEntity<DriverResponseTO> update(@Valid @RequestBody DriverRequestTO driverTO) {
        return new ResponseEntity<>(service.updateDriver(driverTO), HttpStatus.ACCEPTED);
    }

    @PostMapping
    public ResponseEntity<DriverResponseTO> save(@Valid @RequestBody DriverRequestTO driverTO) {
        return new ResponseEntity<>(service.saveDriver(driverTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.softDeleteDriver(id);
    }

    @ExceptionHandler(DriverNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler({DuplicateEmailException.class, DuplicatePhoneException.class})
    public ResponseEntity<Object> handleDuplicateDataException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.CONFLICT, e, request);
    }
}
