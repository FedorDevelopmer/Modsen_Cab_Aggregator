package com.modsen.software.driver.dto;

import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.validation.OnCreate;
import com.modsen.software.driver.validation.OnUpdate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Set;

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
    @Email(groups = {OnUpdate.class, OnCreate.class})
    private String email;

    @NotBlank(groups = {OnUpdate.class, OnCreate.class})
    @Pattern(regexp = "^\\+?[0-9. ()-]{7,25}$",groups = {OnUpdate.class, OnCreate.class})
    private String phoneNumber;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @DecimalMin(value = "1.0")
    private BigDecimal rating;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private Gender gender;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Past(groups = {OnUpdate.class, OnCreate.class})
    private Date birthDate;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private RemoveStatus removeStatus;

    @Valid
    private Set<DriverRelatedCarRequestTO> cars;
}
