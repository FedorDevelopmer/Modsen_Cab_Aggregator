package com.modsen.software.passenger.controller;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.exception.DuplicateEmailException;
import com.modsen.software.passenger.exception.DuplicatePhoneNumberException;
import com.modsen.software.passenger.exception.PassengerNotFoundException;
import com.modsen.software.passenger.exception_handler.ExceptionHandling;
import com.modsen.software.passenger.service.impl.PassengerServiceImpl;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@Controller
@RequestMapping("/api/v1.0/passenger")
public class PassengerController {
    @Autowired
    private PassengerServiceImpl service;

    @GetMapping
    public ResponseEntity<List<PassengerResponseTO>> getAll(@RequestParam(required = false,defaultValue = "0") @Min(0) Integer pageNumber,
                                                         @RequestParam(required = false,defaultValue = "100") @Min(1) Integer pageSize,
                                                         @RequestParam(required = false,defaultValue = "id") String sortBy,
                                                         @RequestParam(required = false,defaultValue = "ASC") String sortOrder){
        List<PassengerResponseTO> passengers = service.getAllPassengers(pageNumber, pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(passengers,HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PassengerResponseTO> findById(@PathVariable @Min(1) Long id){
        PassengerResponseTO passenger = service.findPassengerById(id);
        return new ResponseEntity<>(passenger, HttpStatus.OK);

    }

    @PutMapping
    public ResponseEntity<PassengerResponseTO> update(@Valid  @RequestBody PassengerRequestTO passengerTO){
        return new ResponseEntity<>(service.updatePassenger(passengerTO), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<PassengerResponseTO> save(@Valid  @RequestBody PassengerRequestTO passengerTO){
        return new ResponseEntity<>(service.savePassenger(passengerTO),HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        service.softDeletePassenger(id);
        return new ResponseEntity<>("Passenger successfully deleted(softly)",HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(PassengerNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request){
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND,e,request);
    }

    @ExceptionHandler({DuplicateEmailException.class, DuplicatePhoneNumberException.class})
    public ResponseEntity<Object> handleDuplicationException(RuntimeException e, WebRequest request){
        return ExceptionHandling.formExceptionResponse(HttpStatus.CONFLICT,e,request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(MethodArgumentNotValidException e, WebRequest request){
        return ExceptionHandling.formExceptionResponse(HttpStatus.BAD_REQUEST,e,request);
    }
}
