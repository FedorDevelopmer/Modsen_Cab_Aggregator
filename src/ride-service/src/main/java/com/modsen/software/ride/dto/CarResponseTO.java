package com.modsen.software.ride.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modsen.software.ride.entity.enumeration.Color;
import com.modsen.software.ride.entity.enumeration.RemoveStatus;
import lombok.*;
import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CarResponseTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("driverId")
    private Long driverId;

    @JsonProperty("color")
    private Color color;

    @JsonProperty("brand")
    private String brand;

    @JsonProperty("registrationNumber")
    private String registrationNumber;

    @JsonProperty("inspectionDate")
    private Date inspectionDate;

    @JsonProperty("inspectionDurationMonth")
    private Integer inspectionDurationMonth;

    @JsonProperty("removeStatus")
    private RemoveStatus removeStatus;
}
