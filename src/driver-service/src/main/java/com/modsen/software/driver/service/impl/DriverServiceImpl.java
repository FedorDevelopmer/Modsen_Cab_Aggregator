package com.modsen.software.driver.service.impl;

import com.modsen.software.driver.client.RatingClient;
import com.modsen.software.driver.dto.DriverRelatedCarRequestTO;
import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.dto.RatingEvaluationResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.exception.DriverNotFoundException;
import com.modsen.software.driver.exception.DuplicateEmailException;
import com.modsen.software.driver.exception.DuplicatePhoneException;
import com.modsen.software.driver.exception.InvalidCredentialsException;
import com.modsen.software.driver.filter.DriverFilter;
import com.modsen.software.driver.mapper.CarMapper;
import com.modsen.software.driver.mapper.DriverMapper;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.DriverService;
import com.modsen.software.driver.specification.DriverSpecification;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DriverServiceImpl implements DriverService {

    @Value("${jwt.auth.resource-id}")
    private String resourceId;

    @Autowired
    private DriverRepository repository;

    @Autowired
    private CarRepository carsRepository;

    @Autowired
    private DriverMapper mapper;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private RatingClient ratingClient;

    @Transactional
    public Page<DriverResponseTO> getAllDrivers(DriverFilter filter, Pageable pageable) {
        log.info("Fetching drivers page - page number: {},page size: {}, is first: {}",
                pageable.getPageNumber(), pageable.getPageSize(), !pageable.hasPrevious());
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
        log.info("Fetching driver by id {}", id);
        Optional<Driver> driverOptional = repository.findById(id);
        if (driverOptional.isEmpty()) {
            log.warn("Driver with provided id {} not found", id);
        }
        Driver driver = driverOptional.orElseThrow(DriverNotFoundException::new);
        if (!LocalDateTime.now().isBefore(driver.getRatingUpdateTimestamp().plusDays(1))) {
            updateDriverRating(driver);
        }
        return mapper.driverToResponse(driver);
    }

    @Transactional
    public DriverResponseTO updateDriver(DriverRequestTO driverTO) {
        log.info("Updating driver with id {}", driverTO.getId());
        checkDuplications(driverTO);
        Optional<Driver> oldDriverOptional = repository.findById(driverTO.getId());
        if (oldDriverOptional.isEmpty()) {
            log.warn("Driver for update with provided id {} not found", driverTO.getId());
        }
        Driver oldDriver = oldDriverOptional.orElseThrow(DriverNotFoundException::new);
        Set<Car> cars = oldDriver.getCars();
        Driver driverToUpdate = mapper.requestToDriver(driverTO);
        String email = getUserEmail();
        if ((oldDriver.getEmail().equals(email) && !isAdmin()) || isAdmin()) {
            driverToUpdate.setRatingUpdateTimestamp(oldDriver.getRatingUpdateTimestamp());
            driverToUpdate.setCars(cars);
            driverToUpdate.setRating(oldDriver.getRating());
            if (!LocalDateTime.now().isBefore(oldDriver.getRatingUpdateTimestamp().plusDays(1))) {
                Driver savedDriver = updateDriverRating(driverToUpdate);
                log.info("Update for driver with id {} complete successfully(mean rating updated)", driverTO.getId());
                return mapper.driverToResponse(savedDriver);
            } else {
                Driver savedDriver = repository.save(driverToUpdate);
                log.info("Update for driver with id {} complete successfully(mean rating not updated)", driverTO.getId());
                return mapper.driverToResponse(savedDriver);
            }
        } else {
            log.warn("User and entity email during driver update are different: user email: {}, passenger email {}",
                    email, driverTO.getEmail());
            throw new InvalidCredentialsException("Driver email must be equal to email of user");
        }

    }

    @Transactional
    public void updateDriverByKafka(RatingEvaluationResponseTO ratingEvaluation) {
        log.info("Updating rating of driver with id {} through Kafka", ratingEvaluation.getId());
        Driver driver = repository.findById(ratingEvaluation.getId()).orElseThrow(DriverNotFoundException::new);
        driver.setRating(ratingEvaluation.getMeanEvaluation());
        driver.setRatingUpdateTimestamp(LocalDateTime.now());
        repository.save(driver);
        log.info("Mean rating for driver with id {} is updated through Kafka", ratingEvaluation.getId());
    }

    @Transactional
    public DriverResponseTO saveDriver(DriverRequestTO driverTO) {
        log.info("Saving new driver with email {} and phone number {}",
                driverTO.getEmail(), driverTO.getPhoneNumber());
        checkDuplications(driverTO);
        BigDecimal defaultRating = new BigDecimal(5);
        defaultRating = defaultRating.setScale(2, RoundingMode.HALF_UP);
        if (Objects.nonNull(driverTO.getCars())) {
            log.info("Driver saving with related cars");
            Set<DriverRelatedCarRequestTO> requestCars = Set.copyOf(driverTO.getCars());
            driverTO.getCars().clear();
            Driver driverToSave = mapper.requestToDriver(driverTO);
            String email = getUserEmail();
            if ((driverToSave.getEmail().equals(email) && !isAdmin()) || isAdmin()) {
                driverToSave.setRating(defaultRating);
                driverToSave.setRatingUpdateTimestamp(LocalDateTime.now());
                Driver savedDriver = repository.save(driverToSave);
                log.info("New driver with id {} saved successfully, start saving related cars", savedDriver.getId());
                for (DriverRelatedCarRequestTO relatedCarRequestTO : requestCars) {
                    Car carToSave = carMapper.driverRelatedRequestToCar(relatedCarRequestTO);
                    carToSave.setDriverId(savedDriver.getId());
                    carToSave.setDriver(savedDriver);
                    Car savedCar = carsRepository.save(carToSave);
                    log.info("New car with id {} saved successfully", savedCar.getId());
                    savedDriver.getCars().add(savedCar);
                }
                return mapper.driverToResponse(savedDriver);
            } else {
                log.warn("User and entity email during driver creation with cars are different: user email: {}, passenger email {}",
                        email, driverTO.getEmail());
                throw new InvalidCredentialsException("Driver email must be equal to email of user");
            }

        } else {
            log.info("Driver saving without related cars");
            driverTO.setCars(new HashSet<>());
            Driver driverToSave = mapper.requestToDriver(driverTO);
            String email = getUserEmail();
            if ((driverToSave.getEmail().equals(email) && !isAdmin()) || isAdmin()) {
                driverToSave.setRating(defaultRating);
                driverToSave.setRatingUpdateTimestamp(LocalDateTime.now());
                Driver driver = repository.save(driverToSave);
                log.info("New driver with id {} saved successfully", driver.getId());
                return mapper.driverToResponse(driver);
            } else {
                log.warn("User and entity email during driver creation without cars are different: user email: {}, passenger email {}",
                        email, driverTO.getEmail());
                throw new InvalidCredentialsException("Driver email must be equal to email of user");
            }
        }
    }

    @Transactional
    public void softDeleteDriver(Long id) {
        log.info("Softly deleting driver with id {}", id);
        Optional<Driver> driver = repository.findById(id);
        if (driver.isEmpty()) {
            log.warn("Driver for soft delete with provided id {} not found", id);
            throw new DriverNotFoundException();
        }
        String email = getUserEmail();
        if ((driver.get().getEmail().equals(email) && !isAdmin()) || isAdmin()) {
            driver.get().setRemoveStatus(RemoveStatus.REMOVED);
            updateDriver(mapper.driverToRequest(driver.get()));
            log.info("Soft delete complete for driver with id {}", id);
        } else {
            log.warn("User and entity email during driver delete are different: user email: {}, passenger email {}",
                    email, driver.get().getEmail());
            throw new InvalidCredentialsException("Delete of user data allowed for same account only");
        }

    }

    @Transactional
    public void deleteDriver(Long id) {
        log.info("Deleting driver with id {}", id);
        repository.delete(mapper.responseToDriver(findDriverById(id)));
        log.info("Driver with id {} deleted successfully", id);
    }

    private void checkDuplications(DriverRequestTO driverTO) {
        repository.findByEmail(driverTO.getEmail()).ifPresent(driver -> {
            if (!Objects.equals(driver.getId(), driverTO.getId())) {
                log.warn("Email \"{}\" is duplicated within driver service", driverTO.getEmail());
                throw new DuplicateEmailException();
            }
        });
        repository.findByPhoneNumber(driverTO.getPhoneNumber()).ifPresent(driver -> {
            if (!Objects.equals(driver.getId(), driverTO.getId())) {
                log.warn("Phone number \"{}\" is duplicated within driver service", driverTO.getPhoneNumber());
                throw new DuplicatePhoneException();
            }
        });
    }

    private RatingEvaluationResponseTO evaluateMeanRating(DriverRequestTO driverTO) {
        return ratingClient.evaluateRating("DRIVER", driverTO.getId());
    }

    private Driver updateDriverRating(Driver driver) {
        log.info("Evaluating mean rating for driver with id {} during update", driver.getId());
        RatingEvaluationResponseTO evaluatedRating = evaluateMeanRating(mapper.driverToRequest(driver));
        driver.setRating(evaluatedRating.getMeanEvaluation());
        driver.setRatingUpdateTimestamp(LocalDateTime.now());
        log.info("Mean rating for driver with id {} is up to date", driver.getId());
        return repository.save(driver);
    }

    private String getUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = (Jwt) jwtAuth.getPrincipal();
            String email = jwt.getClaim("email").toString();
            if (email != null) {
                email = jwt.getClaim("email").toString();
            } else {
                log.warn("Field \"email\" is missed in JWT");
                throw new InvalidCredentialsException("Required field of JWT is missed: email");
            }
            return email;
        } else {
            log.warn("Unexpected authentication type during obtaining user email: expected JwtAuthenticationToken, but provided {}", authentication.getClass().getTypeName());
            throw new InvalidCredentialsException("Valid authentication type for application is JWT");
        }
    }

    private boolean isAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            JwtAuthenticationToken jwtAuth = (JwtAuthenticationToken) authentication;
            Jwt jwt = (Jwt) jwtAuth.getPrincipal();
            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
            return Optional.ofNullable(resourceAccess)
                    .map(access -> (Map<String, Object>) access.get(resourceId))
                    .map(clientAccess -> (List<String>) clientAccess.get("roles"))
                    .map(roles -> {
                        if (roles.contains("Admin")) {
                            return true;
                        } else {
                            return false;
                        }
                    })
                    .orElseThrow(() -> {
                        log.warn("Required field of JWT is missed: resource_access or {}", resourceId);
                        throw new InvalidCredentialsException("Format of JWT fields is incorrect for valid token");
                    });
        } else {
            log.warn("Unexpected authentication type during checking user role: expected JwtAuthenticationToken, but provided {}", authentication.getClass().getTypeName());
            throw new InvalidCredentialsException();
        }
    }
}
