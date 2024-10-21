package com.modsen.software.passenger.service;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.filter.PassengerFilter;
import com.modsen.software.passenger.repository.PassengerRepository;
import com.modsen.software.passenger.service.impl.PassengerServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class PassengerServiceTest {

    @MockBean
    private PassengerRepository passengerRepository;

    @InjectMocks
    @Autowired
    private PassengerServiceImpl passengerService;

    private Passenger passenger;

    private Passenger secondPassenger;

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
    @Timeout(1000)
    void testSavePassenger() {
        PassengerRequestTO passengerRequest = new PassengerRequestTO(1L, "Andrew",
                "andrew.tdk@mail.com", "+123-123-123",
                Gender.MALE, RemoveStatus.ACTIVE);
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        PassengerResponseTO savedPassenger = passengerService.savePassenger(passengerRequest);
        assertNotNull(savedPassenger);
        assertEquals(passenger.getId(), savedPassenger.getId());
        Assertions.assertEquals("+123-123-123", savedPassenger.getPhoneNumber());
        Assertions.assertEquals("andrew.tdk@mail.com", savedPassenger.getEmail());
    }

    @Test
    @Timeout(1000)
    void testUpdatePassenger() {
        PassengerRequestTO passengerRequest = new PassengerRequestTO(1L, "Andrew",
                "andrew.tdk@mail.com","+123-123-123",
                Gender.MALE, RemoveStatus.ACTIVE);
        PassengerRequestTO passengerUpdateRequest = new PassengerRequestTO(1L, "Andrew",
                "andrew.tdk@mail.com","+145-121-153",
                Gender.MALE, RemoveStatus.ACTIVE);
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(passengerRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
        passengerService.savePassenger(passengerRequest);
        PassengerResponseTO updatedPassenger = passengerService.updatePassenger(passengerUpdateRequest);
        assertNotNull(updatedPassenger);
        assertEquals(passenger.getId(), updatedPassenger.getId());
        assertEquals("+145-121-153", updatedPassenger.getPhoneNumber());
    }

    @Test
    @Timeout(1000)
    void testSoftDeletePassenger() {
        PassengerRequestTO passengerRequest = new PassengerRequestTO(1L, "Andrew",
                "andrew.tdk@mail.com","+123-123-123",
                Gender.MALE, RemoveStatus.ACTIVE);
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(passengerRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
        PassengerResponseTO savedPassenger = passengerService.savePassenger(passengerRequest);
        passengerService.softDeletePassenger(savedPassenger.getId());
        PassengerResponseTO deletedPassenger = passengerService.findPassengerById(passenger.getId());
        assertNotNull(deletedPassenger);
        assertEquals(savedPassenger.getId(), deletedPassenger.getId());
        assertEquals(RemoveStatus.REMOVED, deletedPassenger.getRemoveStatus());
    }

    @Test
    @Timeout(1000)
    void testFindPassengerById() {
        PassengerRequestTO passengerRequest = new PassengerRequestTO(1L, "Andrew",
                "andrew.tdk@mail.com","+123-123-123",
                Gender.MALE, RemoveStatus.ACTIVE);
        when(passengerRepository.save(any(Passenger.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(passengerRepository.findById(passenger.getId())).thenReturn(Optional.of(passenger));
        passengerService.savePassenger(passengerRequest);
        PassengerResponseTO result = passengerService.findPassengerById(passenger.getId());
        assertNotNull(result);
        assertEquals(passenger.getId(), result.getId());
        Assertions.assertEquals("+123-123-123", passenger.getPhoneNumber());
        Assertions.assertEquals("andrew.tdk@mail.com", passenger.getEmail());
    }

    @Test
    @Timeout(1000)
    void testFindAllPassengers() {
        PassengerFilter filter = new PassengerFilter();
        Pageable pageable = PageRequest.of(0, 10);
        when(passengerRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(Stream.of(passenger, secondPassenger).toList(), pageable, 2));
        Page<PassengerResponseTO> passengersList = passengerService.getAllPassengers(filter, pageable);
        assertNotNull(passengersList);
        assertEquals(2L, passengersList.getTotalElements());
        assertEquals(passenger.getId(), passengersList.getContent().get(0).getId());
        assertEquals(secondPassenger.getId(), passengersList.getContent().get(1).getId());
        assertTrue(passengersList.isFirst());
        assertEquals(10, passengersList.getSize());
    }
}
