package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.validation.OnCreate;
import com.modsen.software.driver.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;

import java.sql.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DriverRequestTO {

    @NotNull(groups = OnUpdate.class)
    @Min(value = 1,groups = OnUpdate.class)
    private Long id;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String name;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String surname;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String email;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String phoneNumber;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private Gender gender;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Past(groups = {OnUpdate.class, OnCreate.class})
    private Date birthDate;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private RemoveStatus removeStatus;
}
