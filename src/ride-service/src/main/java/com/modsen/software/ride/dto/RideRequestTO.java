package com.modsen.software.ride.dto;

import com.modsen.software.ride.entity.enumeration.RideStatus;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RideRequestTO {

    @NotNull
    @Min(1)
    private Long id;

    @NotNull
    @Min(1)
    private Long driverId;

    @NotNull
    @Min(1)
    private Long passengerId;

    @NotNull
    private String departureAddress;

    @NotNull
    private String destinationAddress;

    @NotNull
    private RideStatus rideStatus;

    @NotNull
    @Past
    private LocalDateTime rideOrderTime;

    @NotNull
    @DecimalMin(value = "0.0",inclusive = false)
    private BigDecimal ridePrice;

}
