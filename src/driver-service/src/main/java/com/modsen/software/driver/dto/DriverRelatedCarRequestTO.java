package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DriverRelatedCarRequestTO {

    @NotNull(groups = OnCreate.class)
    private Color color;

    @NotBlank(groups = OnCreate.class)
    private String brand;

    @NotBlank(groups = OnCreate.class)
    @Pattern(regexp = "^[1-7] *TAX *[0-9]{4}$", groups = OnCreate.class)
    private String registrationNumber;

    @NotNull(groups = OnCreate.class)
    @Past(groups = OnCreate.class)
    private Date inspectionDate;

    @NotNull(groups = OnCreate.class)
    @Min(value = 6, groups = OnCreate.class)
    private Integer inspectionDurationMonth;

    @NotNull(groups = OnCreate.class)
    private RemoveStatus removeStatus;

    @Valid
    private Driver driver;
}
