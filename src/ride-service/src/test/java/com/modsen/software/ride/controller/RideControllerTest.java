package com.modsen.software.ride.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.modsen.software.ride.dto.RideRequestTO;
import com.modsen.software.ride.dto.RideResponseTO;
import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.filter.RideFilter;
import com.modsen.software.ride.service.impl.RideServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RideController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles("test")
public class RideControllerTest {
    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper mapper;

    @MockBean
    private RideServiceImpl rideService;

    private Ride ride;

    private Ride secondRide;

    private RideRequestTO rideRequest;

    private RideResponseTO rideResponse;

    private RideResponseTO secondRideResponse;

    @BeforeAll
    static void setUpObjectMapper() {
        mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUpRides() {
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

        rideRequest = new RideRequestTO();
        rideRequest.setId(1L);
        rideRequest.setDriverId(1L);
        rideRequest.setPassengerId(1L);
        rideRequest.setDepartureAddress("Minsk, Blagodzenskaya,11a");
        rideRequest.setDestinationAddress("Minsk, Starovoytova,9");
        rideRequest.setRidePrice(BigDecimal.valueOf(30));
        rideRequest.setRideOrderTime(LocalDateTime.now());
        rideRequest.setRideStatus(RideStatus.CREATED);

        rideResponse = new RideResponseTO();
        rideResponse.setId(1L);
        rideResponse.setDriverId(1L);
        rideResponse.setPassengerId(1L);
        rideResponse.setDepartureAddress("Minsk, Blagodzenskaya,11a");
        rideResponse.setDestinationAddress("Minsk, Starovoytova,9");
        rideResponse.setRidePrice(BigDecimal.valueOf(30));
        rideResponse.setRideOrderTime(LocalDateTime.now());
        rideResponse.setRideStatus(RideStatus.CREATED);

        secondRideResponse = new RideResponseTO();
        secondRideResponse.setId(2L);
        secondRideResponse.setDriverId(1L);
        secondRideResponse.setPassengerId(1L);
        secondRideResponse.setDepartureAddress("Minsk, Starovoytova,9");
        secondRideResponse.setDestinationAddress("Minsk, Blagodzenskaya,11a");
        secondRideResponse.setRidePrice(BigDecimal.valueOf(20));
        secondRideResponse.setRideOrderTime(LocalDateTime.now().plusHours(5));
        secondRideResponse.setRideStatus(RideStatus.ACCEPTED);
    }

    @Test
    @Timeout(1000)
    void testUpdateRide() throws Exception {

        RideRequestTO rideUpdateRequest = new RideRequestTO();
        rideUpdateRequest.setId(1L);
        rideUpdateRequest.setDriverId(1L);
        rideUpdateRequest.setPassengerId(1L);
        rideUpdateRequest.setDepartureAddress("Minsk, Blagodzenskaya,11a");
        rideUpdateRequest.setDestinationAddress("Minsk, Starovoytova,9");
        rideUpdateRequest.setRidePrice(BigDecimal.valueOf(25));
        rideUpdateRequest.setRideOrderTime(LocalDateTime.now());
        rideUpdateRequest.setRideStatus(RideStatus.CREATED);

        RideResponseTO rideUpdateResponse = new RideResponseTO();
        rideUpdateResponse.setId(1L);
        rideUpdateResponse.setDriverId(1L);
        rideUpdateResponse.setPassengerId(1L);
        rideUpdateResponse.setDepartureAddress("Minsk, Blagodzenskaya,11a");
        rideUpdateResponse.setDestinationAddress("Minsk, Starovoytova,9");
        rideUpdateResponse.setRidePrice(BigDecimal.valueOf(25));
        rideUpdateResponse.setRideOrderTime(LocalDateTime.now());
        rideUpdateResponse.setRideStatus(RideStatus.CREATED);

        when(rideService.updateRide(rideUpdateRequest)).thenReturn(rideUpdateResponse);
        mockMvc.perform(put("/api/v1/rides")
                        .content(mapper.writeValueAsString(rideUpdateRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.passengerId").value(1))
                .andExpect(jsonPath("$.ridePrice").value(BigDecimal.valueOf(25)));
        verify(rideService, times(1)).updateRide(rideUpdateRequest);
    }

    @Test
    @Timeout(1000)
    void testUpdateRideStatus() throws Exception {
        when(rideService.updateRideStatus(ride.getId(), ride.getRideStatus()))
                .thenReturn(rideResponse);
        mockMvc.perform(put("/api/v1/rides/status/{id}", 1L)
                        .param("status", RideStatus.CREATED.name())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.rideStatus").value(ride.getRideStatus().name()));
        verify(rideService, times(1)).updateRideStatus(ride.getId(), ride.getRideStatus());
    }

    @Test
    @Timeout(1000)
    void testCreateRide() throws Exception {
        when(rideService.saveRide(rideRequest)).thenReturn(rideResponse);
        mockMvc.perform(post("/api/v1/rides")
                        .content(mapper.writeValueAsString(rideRequest))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(201))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.passengerId").value(1))
                .andExpect(jsonPath("$.rideStatus").value(RideStatus.CREATED.name()));
        verify(rideService, times(1)).saveRide(rideRequest);
    }

    @Test
    @Timeout(1000)
    void testDeleteRide() throws Exception {
        mockMvc.perform(delete("/api/v1/rides/{id}", rideRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(204));

        verify(rideService, times(1)).deleteRide(rideRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testFindRideById() throws Exception {
        when(rideService.findRideById(rideRequest.getId())).thenReturn(rideResponse);
        mockMvc.perform(get("/api/v1/rides/{id}", rideRequest.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.driverId").value(1))
                .andExpect(jsonPath("$.passengerId").value(1));
        verify(rideService, times(1)).findRideById(rideRequest.getId());
    }

    @Test
    @Timeout(1000)
    void testGetAllRides() throws Exception {
        Pageable pageable = PageRequest.of(0, 10, Sort.Direction.ASC, "id");
        ArgumentCaptor<RideFilter> filterCaptor = forClass(RideFilter.class);
        ArgumentCaptor<Pageable> pageableCaptor = forClass(Pageable.class);
        when(rideService.getAllRides(any(RideFilter.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Stream.of(rideResponse, secondRideResponse).toList(),
                        pageable, 2));
        mockMvc.perform(get("/api/v1/rides")
                        .param("size", "10")
                        .param("page", "0")
                        .param("sort", "id,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[1].id").value(2))
                .andExpect(jsonPath("$.content[1].destinationAddress").value("Minsk, Blagodzenskaya,11a"))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
        verify(rideService, times(1)).getAllRides(filterCaptor.capture(),
                pageableCaptor.capture());
    }
}
