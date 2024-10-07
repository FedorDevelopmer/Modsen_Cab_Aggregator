package com.modsen.software.rating.dto;

import com.modsen.software.rating.entity.enumeration.Color;
import com.modsen.software.rating.entity.enumeration.RemoveStatus;
import lombok.*;
import java.sql.Date;

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
