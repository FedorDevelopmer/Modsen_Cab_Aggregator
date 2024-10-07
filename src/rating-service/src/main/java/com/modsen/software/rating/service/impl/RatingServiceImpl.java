package com.modsen.software.rating.service.impl;

import com.modsen.software.rating.client.DriverClient;
import com.modsen.software.rating.client.PassengerClient;
import com.modsen.software.rating.dto.RatingEvaluationResponseTO;
import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.exception.RatingScoreNotFoundException;
import com.modsen.software.rating.filter.RatingScoreFilter;
import com.modsen.software.rating.mapper.RatingScoreMapper;
import com.modsen.software.rating.repository.RatingRepository;
import com.modsen.software.rating.service.RatingService;
import com.modsen.software.rating.specification.RatingScoreSpecification;
import feign.Feign;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import feign.okhttp.OkHttpClient;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
public class RatingServiceImpl implements RatingService {

    private final String DRIVER_SERVICE_URI = "http://localhost:8080/api/v1/drivers";

    private final String PASSENGER_SERVICE_URI = "http://localhost:8081/api/v1/passengers";

    @Autowired
    private RatingRepository repository;

    @Autowired
    private DriverClient driverClient;

    @Autowired
    private PassengerClient passengerClient;

    @Autowired
    private RatingScoreMapper mapper;

    @Transactional
    public Page<RatingScoreResponseTO> getAllRatingScores(RatingScoreFilter filter, Pageable pageable) {
        Specification<RatingScore> spec = Specification.where(RatingScoreSpecification.hasDriverId(filter.getDriverId()))
                .and(RatingScoreSpecification.hasPassengerId(filter.getPassengerId()))
                .and(RatingScoreSpecification.hasEvaluation(filter.getEvaluation()))
                .and(RatingScoreSpecification.hasEvaluationHigher(filter.getEvaluationHigher()))
                .and(RatingScoreSpecification.hasEvaluationLower(filter.getEvaluationLower()))
                .and(RatingScoreSpecification.hasInitiator(filter.getInitiator()));
        return repository.findAll(spec, pageable).map((item) -> mapper.ratingScoreToResponse(item));
    }

    @Transactional
    public RatingScoreResponseTO findRatingScoreById(Long id) {
        Optional<RatingScore> rating = repository.findById(id);
        return mapper.ratingScoreToResponse(rating.orElseThrow(RatingScoreNotFoundException::new));
    }

    @Transactional
    public RatingEvaluationResponseTO evaluateMeanRatingById(Long id, Initiator initiator, Pageable pageable) {
        Specification<RatingScore> spec;
        if (initiator.equals(Initiator.DRIVER)) {
            spec = Specification.where(RatingScoreSpecification.hasDriverId(id));
        } else {
            spec = Specification.where(RatingScoreSpecification.hasPassengerId(id));
        }
        List<RatingScore> scores = repository.findAll(spec, pageable).getContent();
        BigDecimal meanEvaluation = new BigDecimal("0.0");
        meanEvaluation = meanEvaluation.setScale(2, RoundingMode.HALF_UP);
        for (RatingScore ratingScore : scores) {
            meanEvaluation = meanEvaluation.add(BigDecimal.valueOf(ratingScore.getEvaluation()));
        }
        if (scores.size() < pageable.getPageSize()) {
            for (int i = 0; i < pageable.getPageSize() - scores.size(); i++) {
                meanEvaluation = meanEvaluation.add(BigDecimal.valueOf(5.0));
            }
        }
        meanEvaluation = meanEvaluation.divide(BigDecimal.valueOf(50), RoundingMode.HALF_UP);
        return new RatingEvaluationResponseTO(id, meanEvaluation);
    }

    @Transactional
    public RatingScoreResponseTO updateRatingScore(RatingScoreRequestTO ratingTO) {
        driverClient.getDriver(ratingTO.getDriverId());
        passengerClient.getPassenger(ratingTO.getPassengerId());
        repository.findById(ratingTO.getId()).orElseThrow(RatingScoreNotFoundException::new);
        return mapper.ratingScoreToResponse(repository.save(mapper.requestToRatingScore(ratingTO)));
    }

    @Transactional
    public RatingScoreResponseTO saveRatingScore(RatingScoreRequestTO ratingTO) {
        driverClient.getDriver(ratingTO.getDriverId());
        passengerClient.getPassenger(ratingTO.getPassengerId());
        RatingScore saved = repository.save(mapper.requestToRatingScore(ratingTO));
        return mapper.ratingScoreToResponse(saved);
    }

    @Transactional
    public void deleteRatingScore(Long id) {
        repository.delete(mapper.responseToRatingScore(findRatingScoreById(id)));
    }
}
