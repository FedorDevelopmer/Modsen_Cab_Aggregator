package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DriverRequestTO {

    @NotNull
    @Min(1)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String surname;

    @NotBlank
    private String email;

    @NotBlank
    private String phoneNumber;

    @NotNull
    private Gender gender;

    @NotNull
    @Past
    private Date birthDate;

    @NotNull
    private RemoveStatus removeStatus;
}
