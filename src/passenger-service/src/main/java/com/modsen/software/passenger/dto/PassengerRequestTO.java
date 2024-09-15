package com.modsen.software.passenger.dto;

import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import com.modsen.software.passenger.validation.OnCreate;
import com.modsen.software.passenger.validation.OnUpdate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PassengerRequestTO {

    @NotNull(groups = OnUpdate.class)
    @Min(value = 1, groups = OnUpdate.class)
    private Long id;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String name;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String email;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    private String phoneNumber;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private Gender gender;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private RemoveStatus removeStatus;
}
