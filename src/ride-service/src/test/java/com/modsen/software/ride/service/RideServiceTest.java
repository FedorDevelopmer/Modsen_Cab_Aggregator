package com.modsen.software.ride.service;

import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.filter.RideFilter;
import com.modsen.software.ride.repository.RideRepository;
import com.modsen.software.ride.service.impl.RideServiceImpl;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Stream;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class RideServiceTest {

    @MockBean
    private RideRepository rideRepository;

    @MockBean
    private RestClient restClient;

    @InjectMocks
    @Autowired
    private RideServiceImpl rideService;

    private Ride ride;

    private Ride secondRide;

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
    @Timeout(1000)
    void testSaveRide() {

        RideRequestTO rideRequest = new RideRequestTO(1L, 1L,
                1L, "Minsk, Blagodzenskaya,11a",
                "Minsk, Starovoytova,9", RideStatus.CREATED,
                LocalDateTime.now(), BigDecimal.valueOf(30));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        mockRestClient();
        RideResponseTO savedRide = rideService.saveRide(rideRequest);
        assertNotNull(savedRide);
        assertEquals(ride.getId(), savedRide.getId());
        Assertions.assertEquals(1L, savedRide.getDriverId());
        Assertions.assertEquals(1L, savedRide.getPassengerId());
    }

    @Test
    @Timeout(1000)
    void testUpdateRideStatus() {
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        RideResponseTO rideResponse = rideService.updateRideStatus(1L, RideStatus.ACCEPTED);
        assertNotNull(rideResponse);
        assertEquals(1L, rideResponse.getId());
        Assertions.assertEquals(RideStatus.ACCEPTED, rideResponse.getRideStatus());
    }

    @Test
    @Timeout(1000)
    void testUpdateRide() {
        RideRequestTO rideRequest = new RideRequestTO(1L, 1L,
                1L, "Minsk, Blagodzenskaya,11a",
                "Minsk, Starovoytova,9", RideStatus.CREATED,
                LocalDateTime.now(), BigDecimal.valueOf(30));
        RideRequestTO rideUpdateRequest = new RideRequestTO(1L, 1L,
                1L, "Minsk, Blagodzenskaya,11a",
                "Minsk, Starovoytova,9", RideStatus.ACCEPTED,
                LocalDateTime.now(), BigDecimal.valueOf(20));
        mockRestClient();
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        rideService.saveRide(rideRequest);
        RideResponseTO updatedRide = rideService.updateRide(rideUpdateRequest);
        assertNotNull(updatedRide);
        assertEquals(ride.getId(), updatedRide.getId());
        assertEquals(RideStatus.ACCEPTED, updatedRide.getRideStatus());
        assertEquals(BigDecimal.valueOf(20), updatedRide.getRidePrice());
    }

    @Test
    @Timeout(1000)
    void testDeleteRide() {
        RideRequestTO rideRequest = new RideRequestTO(1L, 1L,
                1L, "Minsk, Blagodzenskaya,11a",
                "Minsk, Starovoytova,9", RideStatus.CREATED,
                LocalDateTime.now(), BigDecimal.valueOf(30));
        mockRestClient();
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        RideResponseTO savedRide = rideService.saveRide(rideRequest);
        rideService.deleteRide(savedRide.getId());
    }

    @Test
    @Timeout(1000)
    void testFindRideById() {
        RideRequestTO rideRequest = new RideRequestTO(1L, 1L,
                1L, "Minsk, Blagodzenskaya,11a",
                "Minsk, Starovoytova,9", RideStatus.CREATED,
                LocalDateTime.now(), BigDecimal.valueOf(30));
        mockRestClient();
        when(rideRepository.save(any(Ride.class))).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));
        when(rideRepository.findById(ride.getId())).thenReturn(Optional.of(ride));
        rideService.saveRide(rideRequest);
        RideResponseTO result = rideService.findRideById(ride.getId());
        assertNotNull(result);
        assertEquals(ride.getId(), result.getId());
        Assertions.assertEquals(1L, ride.getDriverId());
        Assertions.assertEquals(1L, ride.getPassengerId());
    }

    @Test
    @Timeout(1000)
    void testFindAllRides() {
        RideFilter filter = new RideFilter();
        Pageable pageable = PageRequest.of(0, 10);
        when(rideRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(new PageImpl<>(Stream.of(ride, secondRide).toList(), pageable, 2));
        Page<RideResponseTO> ridesList = rideService.getAllRides(filter, pageable);
        assertNotNull(ridesList);
        assertEquals(2L, ridesList.getTotalElements());
        assertEquals(ride.getId(), ridesList.getContent().get(0).getId());
        assertEquals(secondRide.getId(), ridesList.getContent().get(1).getId());
        assertTrue(ridesList.isFirst());
        assertEquals(10, ridesList.getSize());
    }

    private void mockRestClient() {
        RestClient.RequestHeadersUriSpec requestHeaders = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);
        when(restClient.get()).thenReturn(requestHeaders);
        when(requestHeaders.uri(anyString(), anyLong())).thenReturn(requestHeaders);
        when(requestHeaders.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);
    }
}
