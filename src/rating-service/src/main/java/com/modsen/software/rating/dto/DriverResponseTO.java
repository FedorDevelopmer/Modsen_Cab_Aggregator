package com.modsen.software.rating.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.modsen.software.rating.entity.enumeration.Gender;
import com.modsen.software.rating.entity.enumeration.RemoveStatus;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Set;

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

    private BigDecimal rating;

    private Gender gender;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private Date birthDate;

    private RemoveStatus removeStatus;

    private Set<CarResponseTO> cars;
}
