package com.modsen.software.passenger.dto;

import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    private LocalDateTime ratingUpdateTimestamp;

    private RemoveStatus removeStatus;
}
