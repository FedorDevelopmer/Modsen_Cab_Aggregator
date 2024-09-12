package com.modsen.software.passenger.dto;

import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import lombok.*;

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

    private Gender gender;

    private RemoveStatus removeStatus;

}
