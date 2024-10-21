package com.modsen.software.ride.repository;

import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.filter.RideFilter;
import com.modsen.software.ride.specification.RideSpecification;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RideRepositoryUnitTest {

    @Autowired
    private RideRepository repository;

    private Ride ride;

    private Ride secondRide;

    private RideFilter filter;

    @BeforeEach
    void setUpRide() {
        ride = Ride.builder()
                .id(1L)
                .driverId(1L)
                .passengerId(1L)
                .departureAddress("Minsk, Blagodzenskaya,11a")
                .destinationAddress("Minsk, Starovoytova,9")
                .ridePrice(BigDecimal.valueOf(30))
                .rideOrderTime(LocalDateTime.now())
                .rideStatus(RideStatus.CREATED)
                .build();

        secondRide = Ride.builder()
                .id(2L)
                .driverId(1L)
                .passengerId(1L)
                .departureAddress("Minsk, Starovoytova,9")
                .destinationAddress("Minsk, Blagodzenskaya,11a")
                .ridePrice(BigDecimal.valueOf(20))
                .rideOrderTime(LocalDateTime.now().plusHours(5))
                .rideStatus(RideStatus.ACCEPTED)
                .build();
    }

    @Test
    void testSaveRide() {
        Ride savedRide = repository.save(ride);
        Assertions.assertNotNull(ride);
        Assertions.assertEquals(1L, savedRide.getId());
        Assertions.assertEquals(1L, savedRide.getDriverId());
        Assertions.assertEquals("Minsk, Blagodzenskaya,11a", savedRide.getDepartureAddress());
        Assertions.assertEquals(BigDecimal.valueOf(30), savedRide.getRidePrice());
    }

    @Test
    void testUpdateRide() {
        repository.save(ride);
        ride.setRidePrice(BigDecimal.valueOf(20));
        ride.setDriverId(2L);
        Ride updatedRide = repository.save(ride);
        Assertions.assertNotNull(updatedRide);
        Assertions.assertEquals(BigDecimal.valueOf(20), updatedRide.getRidePrice());
        Assertions.assertEquals(2L, updatedRide.getDriverId());
    }

    @Test
    void testFindById() {
        repository.save(ride);
        Optional<Ride> foundRide = repository.findById(ride.getId());
        Assertions.assertNotEquals(Optional.empty(), foundRide);
        Assertions.assertEquals(1L, foundRide.get().getDriverId());
    }

    @Test
    void testFindAll() {
        repository.save(ride);
        repository.save(secondRide);
        List<Ride> scores = repository.findAll();
        Assertions.assertNotNull(scores);
        Assertions.assertEquals(2, scores.size());
        Assertions.assertEquals(ride.getId(), scores.get(0).getId());
        Assertions.assertEquals(secondRide.getId(), scores.get(1).getId());
    }

    @Test
    void testFindAllWithFilterByRidePriceLower() {
        repository.save(ride);
        repository.save(secondRide);
        filter = new RideFilter();
        filter.setRidePrice(BigDecimal.valueOf(20));
        Specification<Ride> spec = Specification.where(RideSpecification.hasRidePrice(filter.getRidePrice()));
        List<Ride> scores = repository.findAll(spec);
        Assertions.assertNotNull(scores);
        Assertions.assertEquals(1, scores.size());
        Assertions.assertEquals(secondRide.getId(), scores.get(0).getId());
    }
}
