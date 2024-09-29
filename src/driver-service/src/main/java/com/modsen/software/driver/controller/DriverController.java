package com.modsen.software.driver.controller;

import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateEmailException;
import com.modsen.software.driver.exception.DuplicatePhoneException;
import com.modsen.software.driver.exception_handler.ExceptionHandling;
import com.modsen.software.driver.filter.DriverFilter;
import com.modsen.software.driver.service.impl.DriverServiceImpl;
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
@RequestMapping("/api/v1/drivers")
public class DriverController {
    @Autowired
    private DriverServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<DriverResponseTO>> getAll(@RequestParam(required = false) String name,
                                                         @RequestParam(required = false) String surname,
                                                         @RequestParam(required = false) String email,
                                                         @RequestParam(required = false) String phoneNumber,
                                                         @RequestParam(required = false) Gender gender,
                                                         @RequestParam(required = false) Date birthDateEarlier,
                                                         @RequestParam(required = false) Date birthDate,
                                                         @RequestParam(required = false) Date birthDateLater,
                                                         @RequestParam(required = false) RemoveStatus removeStatus,
                                                         @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        DriverFilter filter = new DriverFilter(name, surname, email, phoneNumber, gender, birthDateEarlier,
                birthDate, birthDateLater, removeStatus);
        Page<DriverResponseTO> drivers = service.getAllDrivers(filter, pageable);
        return new ResponseEntity<>(drivers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverResponseTO> findById(@PathVariable @Min(1) Long id) {
        DriverResponseTO driver = service.findDriverById(id);
        return new ResponseEntity<>(driver, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<DriverResponseTO> update(@Validated(OnUpdate.class) @RequestBody DriverRequestTO driverTO) {
        return new ResponseEntity<>(service.updateDriver(driverTO), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<DriverResponseTO> save(@Validated({OnCreate.class}) @RequestBody DriverRequestTO driverTO) {
        return new ResponseEntity<>(service.saveDriver(driverTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.softDeleteDriver(id);
        return new ResponseEntity<>("Driver was successfully deleted(softly).", HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(DriverNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler({DuplicateEmailException.class, DuplicatePhoneException.class})
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
