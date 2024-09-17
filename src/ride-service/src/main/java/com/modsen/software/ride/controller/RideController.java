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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Controller
@RequestMapping("/api/v1/rides")
public class RideController {
    @Autowired
    private RideServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<RideResponseTO>> getAll(@RequestParam(required = false) Long driverId,
                                                       @RequestParam(required = false) Long passengerId,
                                                       @RequestParam(required = false) String departureAddress,
                                                       @RequestParam(required = false) String destinationAddress,
                                                       @RequestParam(required = false) LocalDateTime rideOrderTime,
                                                       @RequestParam(required = false) LocalDateTime rideOrderTimeEarlier,
                                                       @RequestParam(required = false) LocalDateTime rideOrderTimeLater,
                                                       @RequestParam(required = false) RideStatus rideStatus,
                                                       @RequestParam(required = false) BigDecimal ridePrice,
                                                       @RequestParam(required = false) BigDecimal ridePriceHigher,
                                                       @RequestParam(required = false) BigDecimal ridePriceLower,
                                                       @PageableDefault(sort = "id",direction = Sort.Direction.ASC) Pageable pageable) {
        RideFilter filter = new RideFilter(driverId, passengerId, departureAddress, destinationAddress,
                                           rideOrderTime,rideOrderTimeEarlier,rideOrderTimeLater,rideStatus,
                                           ridePrice,ridePriceLower,ridePriceHigher);
        Page<RideResponseTO> rides = service.getAllRides(filter, pageable);
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
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND, e.getMessage(), request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(MethodArgumentNotValidException e, WebRequest request) {
        String message = String.format("Parameter '%s' is invalid. Validation failed for value: '%s'", Objects.requireNonNull(
                        e.getBindingResult().getFieldError()).getField(),
                        e.getBindingResult().getFieldError().getRejectedValue());
        return ExceptionHandling.formExceptionResponse(HttpStatus.BAD_REQUEST, message, request);
    }
}
