package com.modsen.software.driver.repository;

import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.filter.DriverFilter;
import com.modsen.software.driver.shedule.DriverServiceSchedule;
import com.modsen.software.driver.specification.DriverSpecification;
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
public class DriverRepositoryTest {

    @Autowired
    private DriverRepository repository;

    @MockBean
    private DriverServiceSchedule scheduler;

    private Driver driver;

    private Driver secondDriver;

    private DriverFilter filter;

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
    void testSaveDriver() {
        Driver savedDriver = repository.save(driver);
        Assertions.assertNotNull(driver);
        Assertions.assertEquals(1L, savedDriver.getId());
        Assertions.assertEquals("john.con@mail.com", savedDriver.getEmail());
        Assertions.assertEquals("+123-123-123", savedDriver.getPhoneNumber());
    }

    @Test
    void testUpdateDriver() {
        repository.save(driver);
        driver.setName("Keil");
        driver.setPhoneNumber("+321-321-321");
        Driver updatedDriver = repository.save(driver);
        Assertions.assertNotNull(updatedDriver);
        Assertions.assertEquals("Keil", updatedDriver.getName());
        Assertions.assertEquals("+321-321-321", updatedDriver.getPhoneNumber());
    }

    @Test
    void testSoftDeleteDriver() {
        repository.save(driver);
        driver.setRemoveStatus(RemoveStatus.REMOVED);
        Driver removedDriver = repository.save(driver);
        Assertions.assertNotEquals(Optional.empty(), repository.findById(removedDriver.getId()));
        Assertions.assertEquals(RemoveStatus.REMOVED, removedDriver.getRemoveStatus());
    }

    @Test
    void testFindById() {
        repository.save(driver);
        Optional<Driver> foundDriver = repository.findById(driver.getId());
        Assertions.assertNotEquals(Optional.empty(), foundDriver);
        Assertions.assertEquals("John", foundDriver.get().getName());
    }

    @Test
    void testFindByEmail() {
        repository.save(driver);
        Optional<Driver> foundDriver = repository.findByEmail(driver.getEmail());
        Assertions.assertNotEquals(Optional.empty(), foundDriver);
        Assertions.assertEquals("john.con@mail.com", foundDriver.get().getEmail());
    }

    @Test
    void testFindByPhoneNumber() {
        repository.save(driver);
        Optional<Driver> foundDriver = repository.findByPhoneNumber(driver.getPhoneNumber());
        Assertions.assertNotEquals(foundDriver, Optional.empty());
        Assertions.assertEquals("+123-123-123", foundDriver.get().getPhoneNumber());
    }

    @Test
    void testFindAll() {
        repository.save(driver);
        repository.save(secondDriver);
        List<Driver> drivers = repository.findAll();
        Assertions.assertNotNull(drivers);
        Assertions.assertEquals(2, drivers.size());
        Assertions.assertEquals(driver.getId(), drivers.get(0).getId());
        Assertions.assertEquals(secondDriver.getId(), drivers.get(1).getId());
    }

    @Test
    void testFindAllWithFilterByGender() {
        repository.save(driver);
        repository.save(secondDriver);
        filter = new DriverFilter();
        filter.setGender(Gender.MALE);
        Specification<Driver> spec = Specification.where(DriverSpecification.hasGender(filter.getGender()));
        List<Driver> drivers = repository.findAll(spec);
        Assertions.assertNotNull(drivers);
        Assertions.assertEquals(1, drivers.size());
        Assertions.assertEquals(driver.getId(), drivers.get(0).getId());
    }
}
