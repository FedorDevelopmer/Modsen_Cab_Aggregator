package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import java.sql.Date;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CarResponseTO {

    private Long id;

    private Long driverId;

    private Color color;

    private String brand;

    private String registrationNumber;

    private Date inspectionDate;

    private Integer inspectionDurationMonth;

    private RemoveStatus removeStatus;
}
