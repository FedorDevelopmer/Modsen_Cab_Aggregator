package com.modsen.software.driver.service;

import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.dto.CarResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.filter.CarFilter;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.impl.CarServiceImpl;
import com.modsen.software.driver.shedule.DriverServiceSchedule;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import static org.mockito.ArgumentMatchers.*;
import org.mockito.InjectMocks;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class CarServiceUnitTest {

    @MockBean
    private CarRepository carRepository;

    @MockBean
    private DriverRepository driverRepository;

    @MockBean
    private DriverServiceSchedule scheduler;

    @InjectMocks
    @Autowired
    private CarServiceImpl carService;

    private Car car;

    private Car secondCar;

    private Driver driverStub;

    @BeforeEach
    void setUpCar() {
        car = Car.builder()
                .id(1L)
                .driverId(1L)
                .color(Color.GREEN)
                .brand("Ford")
                .registrationNumber("6TAX789")
                .inspectionDate(new Date(System.currentTimeMillis()))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        secondCar = Car.builder()
                .id(2L)
                .driverId(1L)
                .color(Color.BLUE)
                .brand("Honda")
                .registrationNumber("7TAX456")
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
    @Timeout(1000)
    void testSaveCar() {
        CarRequestTO carRequest = new CarRequestTO(1L, 1L,
                Color.GREEN, "Ford",
                "6TAX789", new Date(System.currentTimeMillis()),
                24, RemoveStatus.REMOVED, null);
        when(carRepository.save(any(Car.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driverStub));
        CarResponseTO savedCar = carService.saveCar(carRequest);
        assertNotNull(savedCar);
        assertEquals(car.getId(), savedCar.getId());
        assertEquals("Ford", savedCar.getBrand());
        assertEquals("6TAX789", savedCar.getRegistrationNumber());
    }

    @Test
    @Timeout(1000)
    void testUpdateCar() {
        CarRequestTO carRequest = new CarRequestTO(1L, 1L,
                Color.GREEN, "Ford",
                "6TAX789", new Date(System.currentTimeMillis()),
                24, RemoveStatus.ACTIVE, null);
        CarRequestTO carUpdateRequest = new CarRequestTO(1L, 1L,
                Color.GREEN, "Ford",
                "6TAX712", new Date(System.currentTimeMillis()),
                24, RemoveStatus.ACTIVE, null);
        when(carRepository.save(any(Car.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driverStub));
        carService.saveCar(carRequest);
        CarResponseTO updatedCar = carService.updateCar(carUpdateRequest);
        assertNotNull(updatedCar);
        assertEquals(car.getId(), updatedCar.getId());
        assertEquals("6TAX712", updatedCar.getRegistrationNumber());
    }

    @Test
    @Timeout(1000)
    void testSoftDeleteCar() {
        CarRequestTO carRequest = new CarRequestTO(1L, 1L,
                Color.GREEN, "Ford",
                "6TAX789", new Date(System.currentTimeMillis()),
                24, RemoveStatus.ACTIVE, null);
        when(carRepository.save(any(Car.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driverStub));
        CarResponseTO savedCar = carService.saveCar(carRequest);
        carService.softDeleteCar(savedCar.getId());
        CarResponseTO deletedCar = carService.findCarById(car.getId());
        assertNotNull(deletedCar);
        assertEquals(savedCar.getId(), deletedCar.getId());
        assertEquals(RemoveStatus.REMOVED, deletedCar.getRemoveStatus());
    }

    @Test
    @Timeout(1000)
    void testFindCarById() {
        CarRequestTO carRequest = new CarRequestTO(1L, 1L,
                Color.GREEN, "Ford",
                "6TAX789", new Date(System.currentTimeMillis()),
                24, RemoveStatus.ACTIVE, null);
        when(carRepository.save(any(Car.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(carRepository.findById(car.getId())).thenReturn(Optional.of(car));
        when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driverStub));
        carService.saveCar(carRequest);
        CarResponseTO result = carService.findCarById(car.getId());
        assertNotNull(result);
        assertEquals(car.getId(), result.getId());
        assertEquals("Ford", result.getBrand());
        assertEquals("6TAX789", result.getRegistrationNumber());
    }

    @Test
    @Timeout(1000)
    void testFindAllCars() {
        CarFilter filter = new CarFilter();
        Pageable pageable = PageRequest.of(0, 10);
        when(carRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(Stream.of(car, secondCar).toList(), pageable, 2));
        when(driverRepository.findById(anyLong())).thenReturn(Optional.of(driverStub));
        Page<CarResponseTO> carsList = carService.getAllCars(filter, pageable);
        assertNotNull(carsList);
        assertEquals(2L, carsList.getTotalElements());
        assertEquals(car.getId(), carsList.getContent().get(0).getId());
        assertEquals(secondCar.getId(), carsList.getContent().get(1).getId());
        assertTrue(carsList.isFirst());
        assertEquals(10, carsList.getSize());
    }
}
