package com.modsen.software.passenger.controller;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.exception.DuplicateEmailException;
import com.modsen.software.passenger.exception.DuplicatePhoneNumberException;
import com.modsen.software.passenger.exception.PassengerNotFoundException;
import com.modsen.software.passenger.exception_handler.ExceptionHandling;
import com.modsen.software.passenger.filter.PassengerFilter;
import com.modsen.software.passenger.service.impl.PassengerServiceImpl;
import com.modsen.software.passenger.validation.OnCreate;
import com.modsen.software.passenger.validation.OnUpdate;
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
import java.util.Objects;

@Controller
@RequestMapping("/api/v1/passengers")
public class PassengerController {
    @Autowired
    private PassengerServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<PassengerResponseTO>> getAll(@RequestParam(required = false) String name,
                                                            @RequestParam(required = false) String email,
                                                            @RequestParam(required = false) String phoneNumber,
                                                            @RequestParam(required = false) Gender gender,
                                                            @RequestParam(required = false) RemoveStatus removeStatus,
                                                            @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        PassengerFilter filter = new PassengerFilter(name,email,phoneNumber,gender,removeStatus);
        Page<PassengerResponseTO> passengers = service.getAllPassengers(filter, pageable);
        return new ResponseEntity<>(passengers, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponseTO> findById(@PathVariable @Min(1) Long id) {
        PassengerResponseTO passenger = service.findPassengerById(id);
        return new ResponseEntity<>(passenger, HttpStatus.OK);

    }

    @PutMapping
    public ResponseEntity<PassengerResponseTO> update(@Validated(OnUpdate.class) @RequestBody PassengerRequestTO passengerTO) {
        return new ResponseEntity<>(service.updatePassenger(passengerTO), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PassengerResponseTO> save(@Validated(OnCreate.class) @RequestBody PassengerRequestTO passengerTO) {
        return new ResponseEntity<>(service.savePassenger(passengerTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.softDeletePassenger(id);
        return new ResponseEntity<>("Passenger successfully deleted(softly)", HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(PassengerNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler({DuplicateEmailException.class, DuplicatePhoneNumberException.class})
    public ResponseEntity<Object> handleDuplicationException(RuntimeException e, WebRequest request) {
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
