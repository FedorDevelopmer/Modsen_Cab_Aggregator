package com.modsen.software.passenger.repository;

import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.filter.PassengerFilter;
import com.modsen.software.passenger.specification.PassengerSpecification;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
public class PassengerRepositoryUnitTest {

    @Autowired
    private PassengerRepository repository;

    private Passenger passenger;

    private Passenger secondPassenger;

    private PassengerFilter filter;

    @BeforeEach
    void setUpPassenger() {
        passenger = Passenger.builder()
                .id(1L)
                .name("Andrew")
                .phoneNumber("+123-123-123")
                .email("andrew.tdk@mail.com")
                .gender(Gender.MALE)
                .rating(BigDecimal.valueOf(5))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
        secondPassenger = Passenger.builder()
                .id(2L)
                .name("Marry")
                .phoneNumber("+123-111-111")
                .email("marry.el@mail.com")
                .gender(Gender.FEMALE)
                .rating(BigDecimal.valueOf(5))
                .ratingUpdateTimestamp(LocalDateTime.now())
                .removeStatus(RemoveStatus.ACTIVE)
                .build();
    }

    @Test
    void testSavePassenger() {
        Passenger savedPassenger = repository.save(passenger);
        Assertions.assertNotNull(passenger);
        Assertions.assertEquals(1L, savedPassenger.getId());
        Assertions.assertEquals("andrew.tdk@mail.com", savedPassenger.getEmail());
        Assertions.assertEquals("Andrew", savedPassenger.getName());
    }

    @Test
    void testUpdatePassenger() {
        repository.save(passenger);
        passenger.setPhoneNumber("+155-143-190");
        passenger.setEmail("andrew.chg@mail.com");
        Passenger updatedPassenger = repository.save(passenger);
        Assertions.assertNotNull(updatedPassenger);
        Assertions.assertEquals("+155-143-190", updatedPassenger.getPhoneNumber());
        Assertions.assertEquals("andrew.chg@mail.com", updatedPassenger.getEmail());
    }

    @Test
    void testSoftDeletePassenger() {
        repository.save(passenger);
        passenger.setRemoveStatus(RemoveStatus.REMOVED);
        Passenger removedPassenger = repository.save(passenger);
        Assertions.assertNotEquals(Optional.empty(), repository.findById(removedPassenger.getId()));
        Assertions.assertEquals(RemoveStatus.REMOVED, removedPassenger.getRemoveStatus());
    }

    @Test
    void testFindById() {
        repository.save(passenger);
        Optional<Passenger> foundPassenger = repository.findById(passenger.getId());
        Assertions.assertNotEquals(Optional.empty(), foundPassenger);
        Assertions.assertEquals("andrew.tdk@mail.com", foundPassenger.get().getEmail());
    }

    @Test
    void testFindAll() {
        repository.save(passenger);
        repository.save(secondPassenger);
        List<Passenger> passengers = repository.findAll();
        Assertions.assertNotNull(passengers);
        Assertions.assertEquals(2, passengers.size());
        Assertions.assertEquals(passenger.getId(), passengers.get(0).getId());
        Assertions.assertEquals(secondPassenger.getId(), passengers.get(1).getId());
    }

    @Test
    void testFindAllWithFilterByGender() {
        repository.save(passenger);
        repository.save(secondPassenger);
        filter = new PassengerFilter();
        filter.setGender(Gender.MALE);
        Specification<Passenger> spec = Specification.where(PassengerSpecification.hasGender(filter.getGender()));
        List<Passenger> passengers = repository.findAll(spec);
        Assertions.assertNotNull(passengers);
        Assertions.assertEquals(1, passengers.size());
        Assertions.assertEquals(passenger.getId(), passengers.get(0).getId());
    }
}
