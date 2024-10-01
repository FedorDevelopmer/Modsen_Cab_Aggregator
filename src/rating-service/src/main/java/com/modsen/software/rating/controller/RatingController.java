package com.modsen.software.rating.controller;

import com.modsen.software.rating.dto.RatingEvaluationResponseTO;
import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.exception.DriverNotFoundException;
import com.modsen.software.rating.exception.PassengerNotFoundException;
import com.modsen.software.rating.exception.RatingScoreNotFoundException;
import com.modsen.software.rating.exception_handler.ExceptionHandling;
import com.modsen.software.rating.filter.RatingScoreFilter;
import com.modsen.software.rating.service.impl.RatingServiceImpl;
import com.modsen.software.rating.validation.OnCreate;
import com.modsen.software.rating.validation.OnUpdate;
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
@RequestMapping("/api/v1/scores")
public class RatingController {
    @Autowired
    private RatingServiceImpl service;

    @GetMapping
    public ResponseEntity<Page<RatingScoreResponseTO>> getAll(@RequestParam(required = false) Long driverId,
                                                              @RequestParam(required = false) Long passengerId,
                                                              @RequestParam(required = false) Integer evaluation,
                                                              @RequestParam(required = false) Integer evaluationLower,
                                                              @RequestParam(required = false) Integer evaluationHigher,
                                                              @RequestParam(required = false) Initiator initiator,
                                                              @PageableDefault(sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        RatingScoreFilter filter = new RatingScoreFilter(driverId, passengerId, evaluation, evaluationHigher, evaluationLower, initiator);
        Page<RatingScoreResponseTO> ratings = service.getAllRatingScores(filter, pageable);
        return new ResponseEntity<>(ratings, HttpStatus.OK);
    }

    @GetMapping("/evaluate/{id}")
    public ResponseEntity<RatingEvaluationResponseTO> evaluateMeanRatingById(@PathVariable @Min(1) Long id,
                                                                             @RequestParam Initiator initiator,
                                                                             @PageableDefault(page = 0, size = 50, sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        RatingEvaluationResponseTO rating = service.evaluateMeanRatingById(id, initiator, pageable);
        return new ResponseEntity<>(rating, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<RatingScoreResponseTO> findById(@PathVariable @Min(1) Long id) {
        RatingScoreResponseTO rating = service.findRatingScoreById(id);
        return new ResponseEntity<>(rating, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<RatingScoreResponseTO> update(@Validated(OnUpdate.class) @RequestBody RatingScoreRequestTO ratingTO) {
        return new ResponseEntity<>(service.updateRatingScore(ratingTO), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<RatingScoreResponseTO> save(@Validated(OnCreate.class) @RequestBody RatingScoreRequestTO ratingTO) {
        return new ResponseEntity<>(service.saveRatingScore(ratingTO), HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        service.deleteRatingScore(id);
        return new ResponseEntity<>("Rating score successfully deleted", HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler({RatingScoreNotFoundException.class, DriverNotFoundException.class, PassengerNotFoundException.class})
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
