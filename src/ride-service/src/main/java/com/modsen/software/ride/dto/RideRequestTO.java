package com.modsen.software.ride.dto;

import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.validation.OnUpdate;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RideRequestTO {

    @NotNull(groups = OnUpdate.class)
    @Min(value = 1, groups = OnUpdate.class)
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
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal ridePrice;

}
