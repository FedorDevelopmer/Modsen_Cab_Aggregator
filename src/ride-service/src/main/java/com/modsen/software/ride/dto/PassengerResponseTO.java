package com.modsen.software.ride.dto;

import com.modsen.software.ride.entity.enumeration.Gender;
import com.modsen.software.ride.entity.enumeration.RemoveStatus;
import lombok.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PassengerResponseTO {

    private Long id;

    private String name;

    private String email;

    private String phoneNumber;

    private BigDecimal rating;

    private Gender gender;

    private RemoveStatus removeStatus;
}
