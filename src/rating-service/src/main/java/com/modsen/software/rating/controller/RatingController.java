package com.modsen.software.rating.controller;

import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.exception.RatingScoreNotFoundException;
import com.modsen.software.rating.exception_handler.ExceptionHandling;
import com.modsen.software.rating.service.impl.RatingServiceImpl;
import com.modsen.software.rating.validation.OnCreate;
import com.modsen.software.rating.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@Controller
@RequestMapping("/api/v1.0/rating")
public class RatingController {
    @Autowired
    private RatingServiceImpl service;

    @GetMapping
    public ResponseEntity<List<RatingScoreResponseTO>> getAll(@RequestParam(required = false, defaultValue = "0") @Min(0) Integer pageNumber,
                                                              @RequestParam(required = false, defaultValue = "100") @Min(1) Integer pageSize,
                                                              @RequestParam(required = false, defaultValue = "id") String sortBy,
                                                              @RequestParam(required = false, defaultValue = "ASC") String sortOrder) {
        List<RatingScoreResponseTO> ratings = service.getAllRides(pageNumber, pageSize, sortBy, sortOrder);
        return new ResponseEntity<>(ratings, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RatingScoreResponseTO> findById(@PathVariable @Min(1) Long id) {
        RatingScoreResponseTO rating = service.findRideById(id);
        return new ResponseEntity<>(rating, HttpStatus.OK);

    }

    @PutMapping
    public ResponseEntity<RatingScoreResponseTO> update(@Validated(OnUpdate.class) @RequestBody RatingScoreRequestTO ratingTO) {
        return new ResponseEntity<>(service.updateRide(ratingTO), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<RatingScoreResponseTO> save(@Validated(OnCreate.class) @RequestBody RatingScoreRequestTO ratingTO) {
        return new ResponseEntity<>(service.saveRide(ratingTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteRide(id);
        return new ResponseEntity<>("Rating score successfully deleted", HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(RatingScoreNotFoundException.class)
    public ResponseEntity<Object> handleNotFoundException(RuntimeException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.NOT_FOUND, e, request);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleInvalidArgumentException(MethodArgumentNotValidException e, WebRequest request) {
        return ExceptionHandling.formExceptionResponse(HttpStatus.BAD_REQUEST, e, request);
    }
}
