package com.modsen.software.rating.service.impl;

import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.exception.RatingScoreNotFoundException;
import com.modsen.software.rating.filter.RatingScoreFilter;
import com.modsen.software.rating.mapper.RatingScoreMapper;
import com.modsen.software.rating.repository.RatingRepository;
import com.modsen.software.rating.service.RatingService;
import com.modsen.software.rating.specification.RatingScoreSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class RatingServiceImpl implements RatingService {
    @Autowired
    private RatingRepository repository;

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
        return repository.findAll(spec,pageable).map((item)-> mapper.ratingScoreToResponse(item));
    }

    @Transactional
    public RatingScoreResponseTO findRatingScoreById(Long id) {
        Optional<RatingScore> rating = repository.findById(id);
        return mapper.ratingScoreToResponse(rating.orElseThrow(RatingScoreNotFoundException::new));
    }

    @Transactional
    public RatingScoreResponseTO updateRatingScore(RatingScoreRequestTO ratingTO) {
        repository.findById(ratingTO.getId()).orElseThrow(RatingScoreNotFoundException::new);
        return mapper.ratingScoreToResponse(repository.save(mapper.requestToRatingScore(ratingTO)));
    }

    @Transactional
    public RatingScoreResponseTO saveRatingScore(RatingScoreRequestTO ratingTO) {
        RatingScore saved = repository.save(mapper.requestToRatingScore(ratingTO));
        return mapper.ratingScoreToResponse(saved);
    }

    @Transactional
    public void deleteRatingScore(Long id) {
        repository.delete(mapper.responseToRatingScore(findRatingScoreById(id)));
    }
}
