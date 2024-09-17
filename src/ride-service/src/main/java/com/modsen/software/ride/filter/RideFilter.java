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

    private LocalDateTime rideOrderTimeEarlier = null;

    private LocalDateTime rideOrderTimeLater = null;

    private RideStatus rideStatus = null;

    private BigDecimal ridePrice = null;

    private BigDecimal ridePriceLower = null;

    private BigDecimal ridePriceHigher = null;
}
