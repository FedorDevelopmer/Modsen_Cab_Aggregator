package com.modsen.software.ride.dto;

import com.modsen.software.ride.entity.enumeration.RideStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RideResponseTO {

    private Long id;

    private Long driverId;

    private Long passengerId;

    private String departureAddress;

    private String destinationAddress;

    private RideStatus rideStatus;

    private LocalDateTime rideOrderTime;

    private BigDecimal ridePrice;

}
