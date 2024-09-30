package com.modsen.software.driver.service;

import com.modsen.software.driver.dto.DriverRelatedCarRequestTO;
import com.modsen.software.driver.dto.DriverRequestTO;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.filter.DriverFilter;
import com.modsen.software.driver.repository.CarRepository;
import com.modsen.software.driver.repository.DriverRepository;
import com.modsen.software.driver.service.impl.DriverServiceImpl;
import com.modsen.software.driver.shedule.DriverServiceSchedule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.InjectMocks;
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
import java.util.Set;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class DriverServiceTest {

    @MockBean
    private DriverRepository driverRepository;

    @MockBean
    private CarRepository carRepository;

    @MockBean
    private DriverServiceSchedule scheduler;

    @InjectMocks
    @Autowired
    private DriverServiceImpl driverService;

    private Driver driver;

    private Driver secondDriver;

    private static Set<DriverRelatedCarRequestTO> cars;

    @BeforeAll
    static void setUpCars() {
        DriverRelatedCarRequestTO carOne = new DriverRelatedCarRequestTO(
                Color.GREEN,
                "Ford",
                "6TAX7898",
                new Date(System.currentTimeMillis()),
                24,
                RemoveStatus.REMOVED,
                null);
        DriverRelatedCarRequestTO carTwo = new DriverRelatedCarRequestTO(
                Color.BLUE,
                "Honda",
                "7TAX4568",
                new Date(System.currentTimeMillis()),
                6,
                RemoveStatus.ACTIVE,
                null);
        cars = new HashSet<>();
        cars.add(carOne);
        cars.add(carTwo);
    }

    @BeforeEach
    void setUpDriver() {
        driver = Driver.builder()
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

        secondDriver = Driver.builder()
                .id(2L)
                .name("Elina")
                .surname("Fallrell")
                .email("el.fall@mail.com")
                .phoneNumber("+111-113-113")
                .birthDate(new Date(System.currentTimeMillis()))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .rating(BigDecimal.valueOf(5))
                .gender(Gender.FEMALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .cars(new HashSet<>())
                .build();
    }

    @Test
    @Timeout(1000)
    void testSaveDriver() {
        Set<DriverRelatedCarRequestTO> carsSaved = Set.copyOf(cars);
        DriverRequestTO driverRequest = new DriverRequestTO(1L, "John",
                "Conor", "john.con@mail.com",
                "+123-123-123",
                Gender.MALE, new Date(System.currentTimeMillis()),
                RemoveStatus.ACTIVE,
                cars);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(carRepository.save(any(Car.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        DriverResponseTO savedDriver = driverService.saveDriver(driverRequest);
        cars = carsSaved;
        assertNotNull(savedDriver);
        assertNotNull(savedDriver.getCars());
        assertEquals(driver.getId(), savedDriver.getId());
        assertEquals("John", savedDriver.getName());
        assertEquals("Conor", savedDriver.getSurname());
        assertEquals("john.con@mail.com", savedDriver.getEmail());
        assertEquals(cars.size(), savedDriver.getCars().size());
    }

    @Test
    @Timeout(1000)
    void testUpdateDriver() {
        DriverRequestTO driverRequest = new DriverRequestTO(1L, "John",
                "Conor", "john.con@mail.com",
                "+123-123-123",
                Gender.MALE, new Date(System.currentTimeMillis()),
                RemoveStatus.ACTIVE,
                cars);
        DriverRequestTO driverUpdateRequest = new DriverRequestTO(1L, "John",
                "Doe", "john2.doe@example.com",
                "+123-456-777",
                Gender.MALE, new Date(System.currentTimeMillis()),
                RemoveStatus.ACTIVE,
                new HashSet<>());
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        driverService.saveDriver(driverRequest);
        DriverResponseTO updatedDriver = driverService.updateDriver(driverUpdateRequest);
        assertNotNull(updatedDriver);
        assertEquals(driver.getId(), updatedDriver.getId());
        assertEquals("john2.doe@example.com", updatedDriver.getEmail());
        assertEquals("+123-456-777", updatedDriver.getPhoneNumber());
    }

    @Test
    @Timeout(1000)
    void testSoftDeleteDriver() {
        DriverRequestTO driverRequest = new DriverRequestTO(1L, "John",
                "Conor", "john.con@mail.com",
                "+123-123-123",
                Gender.MALE, new Date(System.currentTimeMillis()),
                RemoveStatus.ACTIVE,
                cars);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        DriverResponseTO savedDriver = driverService.saveDriver(driverRequest);
        driverService.softDeleteDriver(savedDriver.getId());
        DriverResponseTO deletedDriver = driverService.findDriverById(driver.getId());
        assertNotNull(deletedDriver);
        assertEquals(savedDriver.getId(), deletedDriver.getId());
        assertEquals(RemoveStatus.REMOVED, deletedDriver.getRemoveStatus());
    }

    @Test
    @Timeout(1000)
    void testFindDriverById() {
        DriverRequestTO driverRequest = new DriverRequestTO(1L, "John",
                "Conor", "john.con@mail.com",
                "+123-123-123",
                Gender.MALE, new Date(System.currentTimeMillis()),
                RemoveStatus.ACTIVE,
                cars);
        when(driverRepository.save(any(Driver.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(driverRepository.findById(driver.getId())).thenReturn(Optional.of(driver));
        driverService.saveDriver(driverRequest);
        DriverResponseTO result = driverService.findDriverById(driver.getId());
        assertNotNull(result);
        assertEquals(driver.getId(), result.getId());
        assertEquals("John", result.getName());
        assertEquals("Conor", result.getSurname());
        assertEquals("john.con@mail.com", result.getEmail());
    }

    @Test
    @Timeout(1000)
    void testFindAllDrivers() {

        DriverFilter filter = new DriverFilter();
        Pageable pageable = PageRequest.of(0, 10);
        when(driverRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(Stream.of(driver, secondDriver).toList(), pageable, 2));
        Page<DriverResponseTO> driversList = driverService.getAllDrivers(filter, pageable);
        assertNotNull(driversList);
        assertEquals(2L, driversList.getTotalElements());
        assertEquals(driver.getId(), driversList.getContent().get(0).getId());
        assertEquals(secondDriver.getId(), driversList.getContent().get(1).getId());
        assertTrue(driversList.isFirst());
        assertEquals(10, driversList.getSize());
    }
}
