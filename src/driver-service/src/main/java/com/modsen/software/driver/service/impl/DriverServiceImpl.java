package com.modsen.software.driver.service.impl;

import com.modsen.software.driver.dto.DriverRelatedCarRequestTO;
import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.dto.RatingEvaluationResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.BadEvaluationRequestException;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateEmailException;
import com.modsen.software.driver.exception.DuplicatePhoneException;
import com.modsen.software.driver.filter.DriverFilter;
import com.modsen.software.driver.mapper.CarMapper;
import com.modsen.software.driver.mapper.DriverMapper;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.DriverService;
import com.modsen.software.driver.specification.DriverSpecification;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class DriverServiceImpl implements DriverService {

    @Autowired
    private DriverRepository repository;

    @Autowired
    private CarRepository carsRepository;

    @Autowired
    private DriverMapper mapper;

    @Autowired
    private CarMapper carMapper;

    WebClient ratingClient = WebClient.builder()
            .baseUrl("http://localhost:8083/api/v1/scores")
            .build();

    @Transactional
    public Page<DriverResponseTO> getAllDrivers(DriverFilter filter, Pageable pageable) {
        Specification<Driver> spec = Specification.where(DriverSpecification.hasName(filter.getName()))
                .and(DriverSpecification.hasSurname(filter.getSurname()))
                .and(DriverSpecification.hasEmail(filter.getEmail()))
                .and(DriverSpecification.hasPhone(filter.getPhoneNumber()))
                .and(DriverSpecification.hasGender(filter.getGender()))
                .and(DriverSpecification.hasBirthDateEarlier(filter.getBirthDateEarlier()))
                .and(DriverSpecification.hasBirthDate(filter.getBirthDate()))
                .and(DriverSpecification.hasBirthDateLater(filter.getBirthDateLater()))
                .and(DriverSpecification.hasRemoveStatus(filter.getRemoveStatus()));
        return repository.findAll(spec, pageable)
                .map((item) -> {
                    if (!LocalDateTime.now().isBefore(item.getRatingUpdateTimestamp().plusDays(1))) {
                        updateDriverRating(item);
                    }
                    return item;
                })
                .map((item) -> mapper.driverToResponse(item));
    }

    @Transactional
    public DriverResponseTO findDriverById(Long id) {
        Driver driver = repository.findById(id).orElseThrow(DriverNotFoundException::new);
        if (!LocalDateTime.now().isBefore(driver.getRatingUpdateTimestamp().plusDays(1))) {
            updateDriverRating(driver);
        }
        return mapper.driverToResponse(driver);
    }

    @Transactional
    public DriverResponseTO updateDriver(DriverRequestTO driverTO) {
        checkDuplications(driverTO);
        Driver oldDriver = repository.findById(driverTO.getId()).orElseThrow(DriverNotFoundException::new);
        Set<Car> cars = oldDriver.getCars();
        Driver driverToUpdate = mapper.requestToDriver(driverTO);
        driverToUpdate.setRatingUpdateTimestamp(oldDriver.getRatingUpdateTimestamp());
        driverToUpdate.setCars(cars);
        driverToUpdate.setRating(oldDriver.getRating());
        if (!LocalDateTime.now().isBefore(oldDriver.getRatingUpdateTimestamp().plusDays(1))) {
            return mapper.driverToResponse(updateDriverRating(driverToUpdate));
        } else {
            return mapper.driverToResponse(repository.save(driverToUpdate));
        }
    }

    @Transactional
    public void updateDriverByKafka(RatingEvaluationResponseTO ratingEvaluation) {
        Driver driver = repository.findById(ratingEvaluation.getId()).orElseThrow(DriverNotFoundException::new);
        driver.setRating(ratingEvaluation.getMeanEvaluation());
        driver.setRatingUpdateTimestamp(LocalDateTime.now());
        repository.save(driver);
    }

    @Transactional
    public DriverResponseTO saveDriver(DriverRequestTO driverTO) {
        checkDuplications(driverTO);
        BigDecimal defaultRating = new BigDecimal(5);
        defaultRating = defaultRating.setScale(2, RoundingMode.HALF_UP);
        if (Objects.nonNull(driverTO.getCars())) {
            Set<DriverRelatedCarRequestTO> requestCars = Set.copyOf(driverTO.getCars());
            driverTO.getCars().clear();
            Driver driverToSave = mapper.requestToDriver(driverTO);
            driverToSave.setRating(defaultRating);
            driverToSave.setRatingUpdateTimestamp(LocalDateTime.now());
            Driver savedDriver = repository.save(driverToSave);
            for (DriverRelatedCarRequestTO relatedCarRequestTO : requestCars) {
                Car carToSave = carMapper.driverRelatedRequestToCar(relatedCarRequestTO);
                carToSave.setDriverId(savedDriver.getId());
                carToSave.setDriver(savedDriver);
                savedDriver.getCars().add(carsRepository.save(carToSave));
            }
            return mapper.driverToResponse(savedDriver);
        } else {
            driverTO.setCars(new HashSet<>());
            Driver driverToSave = mapper.requestToDriver(driverTO);
            driverToSave.setRating(defaultRating);
            driverToSave.setRatingUpdateTimestamp(LocalDateTime.now());
            Driver driver = repository.save(driverToSave);
            return mapper.driverToResponse(driver);
        }
    }

    @Transactional
    public void softDeleteDriver(Long id) {
        Optional<Driver> driver = repository.findById(id);
        driver.orElseThrow(DriverNotFoundException::new).setRemoveStatus(RemoveStatus.REMOVED);
        updateDriver(mapper.driverToRequest(driver.get()));
    }

    @Transactional
    public void deleteDriver(Long id) {
        repository.delete(mapper.responseToDriver(findDriverById(id)));
    }

    private void checkDuplications(DriverRequestTO driverTO) {
        repository.findByEmail(driverTO.getEmail()).ifPresent(driver -> {
            if (!Objects.equals(driver.getId(), driverTO.getId())) {
                throw new DuplicateEmailException();
            }
        });
        repository.findByPhoneNumber(driverTO.getPhoneNumber()).ifPresent(driver -> {
            if (!Objects.equals(driver.getId(), driverTO.getId())) {
                throw new DuplicatePhoneException();
            }
        });
    }

    private RatingEvaluationResponseTO evaluateMeanRating(DriverRequestTO driverTO) {
        return ratingClient.get()
                .uri("/evaluate/{id}?initiator=DRIVER", driverTO.getId())
                .retrieve()
                .onStatus(status -> status.isSameCodeAs(HttpStatusCode.valueOf(404)), response -> {
                    throw new DriverNotFoundException();
                })
                .onStatus(status -> status.isSameCodeAs(HttpStatusCode.valueOf(400)), response -> {
                    throw new BadEvaluationRequestException();
                })
                .bodyToMono(RatingEvaluationResponseTO.class)
                .block();
    }

    private Driver updateDriverRating(Driver driver) {
        RatingEvaluationResponseTO evaluatedRating = evaluateMeanRating(mapper.driverToRequest(driver));
        driver.setRating(evaluatedRating.getMeanEvaluation());
        driver.setRatingUpdateTimestamp(LocalDateTime.now());
        return repository.save(driver);
    }
}
