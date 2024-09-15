package com.modsen.software.ride.filter;

import com.modsen.software.ride.entity.enumeration.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RideFilter {

    private Long driverId = null;

    private Long passengerId = null;

    private String departureAddress = null;

    private String destinationAddress = null;

    private LocalDateTime rideOrderTime = null;

    private LocalDateTime rideOrderTimeAndEarlier = null;

    private LocalDateTime rideOrderTimeAndLater = null;

    private RideStatus rideStatus = null;

    private BigDecimal ridePrice = null;

    private BigDecimal ridePriceAndLower = null;

    private BigDecimal ridePriceAndHigher = null;
}
