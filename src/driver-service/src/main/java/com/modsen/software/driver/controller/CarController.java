package com.modsen.software.driver.controller;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.exception.CarNotFoundException;
import com.modsen.software.driver.exception.DuplicateRegistrationNumberException;
import com.modsen.software.driver.exception_handler.ExceptionHandling;
import com.modsen.software.driver.service.impl.CarServiceImpl;
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
@RequestMapping("/api/v1.0/car")
public class CarController {
    @Autowired
    private CarServiceImpl service;

    @GetMapping
    public ResponseEntity<List<CarResponseTO>> getAll(@RequestParam(required = false, defaultValue = "0") @Min(0) Integer pageNumber,
                                                      @RequestParam(required = false, defaultValue = "100") @Min(1) Integer pageSize,
                                                      @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                      @RequestParam(required = false, defaultValue = "ASC") String sortOrder) {
        List<CarResponseTO> cars = service.getAllCars(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(cars, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponseTO> findById(@PathVariable @Min(1) Long id) {
        CarResponseTO car = service.findCarById(id);
        return new ResponseEntity<>(car, HttpStatus.OK);

    }

    @PutMapping
    public ResponseEntity<CarResponseTO> update(@Valid @RequestBody CarRequestTO carTO) {
        CarResponseTO updatedCar = service.updateCar(carTO);
        return new ResponseEntity<>(updatedCar, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CarResponseTO> save(@Valid @RequestBody CarRequestTO carTO) {
        return new ResponseEntity<>(service.saveCar(carTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.softDeleteCar(id);
    }

    @ExceptionHandler(CarNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler(DuplicateRegistrationNumberException.class)
    public ResponseEntity<Object> handleDuplicateDataException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.CONFLICT, e, request);
    }

}
