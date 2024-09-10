package com.modsen.software.ride.entity;

import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.service.RideService;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "rides")
public class Ride {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ride_id")
    private Long id;

    @Column(name = "ride_driver_id")
    private Long driverId;

    @Column(name = "ride_passenger_id")
    private Long passengerId;

    @Column(name = "ride_departure_address")
    private String departureAddress;

    @Column(name = "ride_destination_address")
    private String destinationAddress;

    @Column(name = "ride_order_time")
    private LocalDateTime rideOrderTime;

    @Column(name = "ride_status")
    private RideStatus rideStatus;

    @Column(name = "ride_price")
    private BigDecimal ridePrice;
}
