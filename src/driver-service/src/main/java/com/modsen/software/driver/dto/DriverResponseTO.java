package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import lombok.*;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DriverResponseTO {

    private Long id;

    private String name;

    private String surname;

    private String email;

    private String phoneNumber;

    private Gender gender;

    private Date birthDate;

    private RemoveStatus removeStatus;

}
