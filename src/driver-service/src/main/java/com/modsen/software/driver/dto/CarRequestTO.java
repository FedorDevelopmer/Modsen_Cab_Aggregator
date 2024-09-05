package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.validation.annotation.Validated;

import java.sql.Date;
import java.sql.Time;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CarRequestTO {

    @NotNull
    @Min(1)
    private Long id;

    @NotNull
    @Min(1)
    private Long driverId;

    @NotNull
    private Color color;

    @NotBlank
    private String brand;

    @NotBlank
    private String registrationNumber;

    @NotNull
    @Past
    private Date inspectionDate;

    @NotNull
    @Min(6)
    private Integer inspectionDurationMonth;

    @NotNull
    private RemoveStatus removeStatus;
}
