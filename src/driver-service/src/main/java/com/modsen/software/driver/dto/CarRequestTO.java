package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.validation.OnCreate;
import com.modsen.software.driver.validation.OnUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.validation.annotation.Validated;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Validated
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
    @Pattern(regexp = "^[1-7] *TAX *[0-9]{4}$",groups = {OnUpdate.class, OnCreate.class})
    private String registrationNumber;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Past(groups = {OnUpdate.class, OnCreate.class})
    private Date inspectionDate;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Min(value = 6,groups = {OnUpdate.class, OnCreate.class})
    private Integer inspectionDurationMonth;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private RemoveStatus removeStatus;

    @Valid
    private Driver driver;
}
