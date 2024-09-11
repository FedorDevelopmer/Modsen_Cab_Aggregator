package com.modsen.software.ride.controller;

import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.exception.RideNotFoundException;
import com.modsen.software.ride.exception_handler.ExceptionHandling;
import com.modsen.software.ride.service.impl.RideServiceImpl;
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
@RequestMapping("/api/v1.0/ride")
public class RideController {
    @Autowired
    private RideServiceImpl service;

    @GetMapping
    public ResponseEntity<List<RideResponseTO>> getAll(@RequestParam(required = false,defaultValue = "0") @Min(0) Integer pageNumber,
                                                         @RequestParam(required = false,defaultValue = "100") @Min(1) Integer pageSize,
                                                         @RequestParam(required = false,defaultValue = "id") String sortBy,
                                                         @RequestParam(required = false,defaultValue = "ASC") String sortOrder){
        List<RideResponseTO> rides = service.getAllRides(pageNumber, pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(rides,HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponseTO> findById(@PathVariable @Min(1) Long id){
        RideResponseTO ride = service.findRideById(id);
        return new ResponseEntity<>(ride, HttpStatus.OK);

    }

    @PutMapping
    public ResponseEntity<RideResponseTO> update(@Valid  @RequestBody RideRequestTO rideTO){
        return new ResponseEntity<>(service.updateRide(rideTO), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<RideResponseTO> save(@Valid  @RequestBody RideRequestTO rideTO){
        return new ResponseEntity<>(service.saveRide(rideTO),HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id){
        service.deleteRide(id);
        return new ResponseEntity<>("Ride successfully deleted",HttpStatus.NO_CONTENT);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<RideResponseTO> updateStatus(@PathVariable Long id, @RequestParam RideStatus status){
        return new ResponseEntity<>(service.updateRideStatus(id,status),HttpStatus.OK);
    }

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request){
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND,e,request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(MethodArgumentNotValidException e, WebRequest request){
        return ExceptionHandling.formExceptionResponse(HttpStatus.BAD_REQUEST,e,request);
    }
}
