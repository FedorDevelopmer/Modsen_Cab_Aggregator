package com.modsen.software.rating.service.impl;

import com.modsen.software.rating.dto.RatingScoreRequestTO;
import com.modsen.software.rating.dto.RatingScoreResponseTO;
import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.exception.RatingScoreNotFoundException;
import com.modsen.software.rating.mapper.RatingScoreMapper;
import com.modsen.software.rating.repository.RatingRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RatingServiceImpl {
    @Autowired
    private RatingRepository repository;

    @Autowired
    private RatingScoreMapper mapper;

    @Transactional
    public List<RatingScoreResponseTO> getAllRides(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        return repository.findAll(PageRequest.of(pageNumber, pageSize,
                        Sort.by(Sort.Direction.valueOf(sortOrder), sortBy)))
                .stream()
                .map(mapper::ratingToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public RatingScoreResponseTO findRideById(Long id) {
        Optional<RatingScore> rating = repository.findById(id);
        return mapper.ratingToResponse(rating.orElseThrow(RatingScoreNotFoundException::new));
    }

    @Transactional
    public RatingScoreResponseTO updateRide(RatingScoreRequestTO ratingTO) {
        repository.findById(ratingTO.getId()).orElseThrow(RatingScoreNotFoundException::new);
        return mapper.ratingToResponse(repository.save(mapper.requestToRide(ratingTO)));
    }

    @Transactional
    public RatingScoreResponseTO saveRide(RatingScoreRequestTO ratingTO) {
        RatingScore saved = repository.save(mapper.requestToRide(ratingTO));
        return mapper.ratingToResponse(saved);
    }

    @Transactional
    public void deleteRide(Long id) {
        repository.delete(mapper.responseToRide(findRideById(id)));
    }
}
