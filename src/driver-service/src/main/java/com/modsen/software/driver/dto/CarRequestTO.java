package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.validation.OnCreate;
import com.modsen.software.driver.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CarRequestTO {

    @NotNull(groups = OnUpdate.class)
    @Min(value = 1,groups = OnUpdate.class)
    private Long id;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Min(value = 1,groups = {OnUpdate.class, OnCreate.class})
    private Long driverId;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private Color color;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String brand;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String registrationNumber;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Past(groups = {OnUpdate.class, OnCreate.class})
    private Date inspectionDate;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Min(value = 6,groups = {OnUpdate.class, OnCreate.class})
    private Integer inspectionDurationMonth;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private RemoveStatus removeStatus;
}
