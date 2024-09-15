package com.modsen.software.ride.controller;

import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.exception.RideNotFoundException;
import com.modsen.software.ride.exception_handler.ExceptionHandling;
import com.modsen.software.ride.filter.RideFilter;
import com.modsen.software.ride.service.impl.RideServiceImpl;
import com.modsen.software.ride.validation.OnCreate;
import com.modsen.software.ride.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/api/v1/rides")
public class RideController {
    @Autowired
    private RideServiceImpl service;

    @GetMapping
    public ResponseEntity<List<RideResponseTO>> getAll(@RequestBody(required = false) Optional<RideFilter> filter,
                                                       @PageableDefault(sort = "id",direction = Sort.Direction.ASC) Pageable pageable) {
        List<RideResponseTO> rides = filter.isPresent() ? service.getAllRides(filter.get(),pageable) : service.getAllRides(new RideFilter(),pageable);
        return new ResponseEntity<>(rides, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RideResponseTO> findById(@PathVariable @Min(1) Long id) {
        RideResponseTO ride = service.findRideById(id);
        return new ResponseEntity<>(ride, HttpStatus.OK);

    }

    @PutMapping
    public ResponseEntity<RideResponseTO> update(@Validated(OnUpdate.class) @RequestBody RideRequestTO rideTO) {
        return new ResponseEntity<>(service.updateRide(rideTO), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<RideResponseTO> save(@Validated(OnCreate.class) @RequestBody RideRequestTO rideTO) {
        return new ResponseEntity<>(service.saveRide(rideTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteRide(id);
        return new ResponseEntity<>("Ride successfully deleted", HttpStatus.NO_CONTENT);
    }

    @PutMapping("/status/{id}")
    public ResponseEntity<RideResponseTO> updateStatus(@PathVariable Long id, @RequestParam RideStatus status) {
        return new ResponseEntity<>(service.updateRideStatus(id, status), HttpStatus.OK);
    }

    @ExceptionHandler(RideNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(MethodArgumentNotValidException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.BAD_REQUEST, e, request);
    }
}
