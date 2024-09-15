package com.modsen.software.ride.dto;

import com.modsen.software.ride.entity.enumeration.RideStatus;
import com.modsen.software.ride.validation.OnCreate;
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

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Min(value = 1, groups = {OnUpdate.class, OnCreate.class})
    private Long driverId;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Min(value = 1, groups = OnCreate.class)
    private Long passengerId;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private String departureAddress;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private String destinationAddress;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private RideStatus rideStatus;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Past(groups = {OnUpdate.class, OnCreate.class})
    private LocalDateTime rideOrderTime;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @DecimalMin(value = "0.0", inclusive = false, groups = {OnUpdate.class, OnCreate.class})
    private BigDecimal ridePrice;

}
