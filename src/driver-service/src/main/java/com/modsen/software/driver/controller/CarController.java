package com.modsen.software.driver.controller;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.CarNotFoundException;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateRegistrationNumberException;
import com.modsen.software.driver.exception_handler.ExceptionHandling;
import com.modsen.software.driver.filter.CarFilter;
import com.modsen.software.driver.service.impl.CarServiceImpl;
import com.modsen.software.driver.validation.OnCreate;
import com.modsen.software.driver.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import java.sql.Date;
import java.util.Objects;

@Controller
@RequestMapping("/api/v1/cars")
public class CarController {
    @Autowired
    private CarServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<CarResponseTO>> getAll(@RequestParam(required = false) Color color,
                                                      @RequestParam(required = false) String brand,
                                                      @RequestParam(required = false) String registrationNumber,
                                                      @RequestParam(required = false) Date inspectionDateEarlier,
                                                      @RequestParam(required = false) Date inspectionDate,
                                                      @RequestParam(required = false) Date inspectionDateLater,
                                                      @RequestParam(required = false) Integer inspectionDurationMonth,
                                                      @RequestParam(required = false) RemoveStatus removeStatus,
                                                      @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        CarFilter filter = new CarFilter(color, brand, registrationNumber, inspectionDateEarlier,
                inspectionDate, inspectionDateLater, inspectionDurationMonth,
                removeStatus);
        Page<CarResponseTO> cars = service.getAllCars(filter, pageable);
        return new ResponseEntity<>(cars, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarResponseTO> findById(@PathVariable @Min(1) Long id) {
        CarResponseTO car = service.findCarById(id);
        return new ResponseEntity<>(car, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<CarResponseTO> update(@Validated(OnUpdate.class) @RequestBody CarRequestTO carTO) {
        CarResponseTO updatedCar = service.updateCar(carTO);
        return new ResponseEntity<>(updatedCar, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CarResponseTO> save(@Validated(OnCreate.class) @RequestBody CarRequestTO carTO) {
        return new ResponseEntity<>(service.saveCar(carTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.softDeleteCar(id);
        return new ResponseEntity<>("Car was successfully delete(softly).", HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler({CarNotFoundException.class, DriverNotFoundException.class})
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(DuplicateRegistrationNumberException.class)
    public ResponseEntity<Object> handleDuplicateDataException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.BAD_REQUEST, e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(MethodArgumentNotValidException e, WebRequest request) {
        String message = String.format("Parameter '%s' is invalid. Validation failed for value: '%s'", Objects.requireNonNull(
                        e.getBindingResult().getFieldError()).getField(),
                e.getBindingResult().getFieldError().getRejectedValue());
        return ExceptionHandling.formExceptionResponse(HttpStatus.BAD_REQUEST, message, request);
    }
}
