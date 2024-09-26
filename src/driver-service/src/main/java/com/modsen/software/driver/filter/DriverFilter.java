package com.modsen.software.driver.filter;

import com.modsen.software.driver.entity.enumeration.Gender;
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
public class DriverFilter {

    private String name = null;

    private String surname = null;

    private String email = null;

    private String phoneNumber = null;

    private Gender gender = null;

    private Date birthDateEarlier = null;

    private Date birthDate = null;

    private Date birthDateLater = null;

    private RemoveStatus removeStatus = null;
}
