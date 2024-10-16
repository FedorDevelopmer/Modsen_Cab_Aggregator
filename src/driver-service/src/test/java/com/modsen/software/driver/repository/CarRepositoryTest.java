package com.modsen.software.driver.repository;

import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.filter.CarFilter;
import com.modsen.software.driver.shedule.DriverServiceSchedule;
import com.modsen.software.driver.specification.CarSpecification;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class CarRepositoryTest {

    @Autowired
    private CarRepository repository;

    @MockBean
    private DriverServiceSchedule scheduler;

    private Car car;

    private Car secondCar;

    private Driver driverStub;

    private CarFilter filter;

    @BeforeEach
    void setUpCar() {
        car = Car.builder()
                .id(1L)
                .driverId(1L)
                .color(Color.GREEN)
                .brand("Ford")
                .registrationNumber("6TAX7898")
                .inspectionDate(new Date(System.currentTimeMillis()))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        secondCar = Car.builder()
                .id(2L)
                .driverId(1L)
                .color(Color.BLUE)
                .brand("Honda")
                .registrationNumber("7TAX4568")
                .inspectionDate(new Date(System.currentTimeMillis()))
                .inspectionDurationMonth(6)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        driverStub = Driver.builder()
                .id(1L)
                .name("John")
                .surname("Conor")
                .email("john.con@mail.com")
                .phoneNumber("+123-123-123")
                .birthDate(new Date(System.currentTimeMillis()))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .rating(BigDecimal.valueOf(5))
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .cars(new HashSet<>())
                .build();
    }

    @Test
    void testSaveCar() {
        Car savedCar = repository.save(car);
        Assertions.assertNotNull(car);
        Assertions.assertEquals(1L, savedCar.getId());
        Assertions.assertEquals("Ford", savedCar.getBrand());
        Assertions.assertEquals("6TAX7898", savedCar.getRegistrationNumber());
    }

    @Test
    void testUpdateCar() {
        repository.save(car);
        car.setBrand("Renault");
        car.setRegistrationNumber("6TAX7338");
        Car updatedCar = repository.save(car);
        Assertions.assertNotNull(updatedCar);
        Assertions.assertEquals("Renault", updatedCar.getBrand());
        Assertions.assertEquals("6TAX7338", updatedCar.getRegistrationNumber());
    }

    @Test
    void testSoftDeleteCar() {
        repository.save(car);
        car.setRemoveStatus(RemoveStatus.REMOVED);
        Car removedCar = repository.save(car);
        Assertions.assertNotEquals(Optional.empty(), repository.findById(removedCar.getId()));
        Assertions.assertEquals(RemoveStatus.REMOVED, removedCar.getRemoveStatus());
    }

    @Test
    void testFindById() {
        repository.save(car);
        Optional<Car> foundCar = repository.findById(car.getId());
        Assertions.assertNotEquals(Optional.empty(), foundCar);
        Assertions.assertEquals("6TAX7898", foundCar.get().getRegistrationNumber());
    }

    @Test
    void testFindByRegistrationNumber() {
        repository.save(car);
        Optional<Car> foundCar = repository.findByRegistrationNumber(car.getRegistrationNumber());
        Assertions.assertNotEquals(Optional.empty(), foundCar);
        Assertions.assertEquals("6TAX7898", foundCar.get().getRegistrationNumber());
    }

    @Test
    void testFindAll() {
        repository.save(car);
        repository.save(secondCar);
        List<Car> cars = repository.findAll();
        Assertions.assertNotNull(cars);
        Assertions.assertEquals(2, cars.size());
        Assertions.assertEquals(car.getId(), cars.get(0).getId());
        Assertions.assertEquals(secondCar.getId(), cars.get(1).getId());
    }

    @Test
    void testFindAllWithFilterByInspectionDate() {
        repository.save(car);
        repository.save(secondCar);
        filter = new CarFilter();
        filter.setInspectionDate(new Date(System.currentTimeMillis()));
        Specification<Car> spec = Specification.where(CarSpecification.hasInspectionDate(filter.getInspectionDateEarlier()));
        List<Car> cars = repository.findAll(spec);
        Assertions.assertNotNull(cars);
        Assertions.assertEquals(2, cars.size());
        Assertions.assertEquals(car.getId(), cars.get(0).getId());
    }
}
