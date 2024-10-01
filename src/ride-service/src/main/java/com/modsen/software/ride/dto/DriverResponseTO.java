package com.modsen.software.ride.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.modsen.software.ride.entity.enumeration.Gender;
import com.modsen.software.ride.entity.enumeration.RemoveStatus;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Set;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponseTO {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("surname")
    private String surname;

    @JsonProperty("email")
    private String email;

    @JsonProperty("phoneNumber")
    private String phoneNumber;

    @JsonProperty("rating")
    private BigDecimal rating;

    @JsonProperty("gender")
    private Gender gender;

    @JsonProperty("birthDate")
    private Date birthDate;

    @JsonProperty("removeStatus")
    private RemoveStatus removeStatus;

    @JsonProperty("cars")
    private Set<CarResponseTO> cars;
}
