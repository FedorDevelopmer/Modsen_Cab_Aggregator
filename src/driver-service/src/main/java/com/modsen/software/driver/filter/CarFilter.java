package com.modsen.software.driver.filter;

import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarFilter {

    private Color color = null;

    private String brand = null;

    private String registrationNumber = null;

    private Date inspectionDateEarlier = null;

    private Date inspectionDate = null;

    private Date inspectionDateLater = null;

    private Integer inspectionDurationMonth = null;

    private RemoveStatus removeStatus = null;
}
