package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.*;

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

    private Date birthDate;

    private LocalDateTime ratingUpdateTimestamp;

    private RemoveStatus removeStatus;

    private Set<CarResponseTO> cars;
}
