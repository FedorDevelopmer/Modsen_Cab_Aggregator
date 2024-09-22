package com.modsen.software.rating.dto;

import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.validation.OnCreate;
import com.modsen.software.rating.validation.OnUpdate;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RatingScoreRequestTO {

    @NotNull(groups = {OnUpdate.class})
    @Min(value = 1, groups = {OnUpdate.class})
    private Long id;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Min(value = 1, groups = {OnUpdate.class, OnCreate.class})
    private Long driverId;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Min(value = 1, groups = {OnUpdate.class, OnCreate.class})
    private Long passengerId;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    @Min(value = 1, groups = {OnUpdate.class, OnCreate.class})
    @Max(value = 5, groups = {OnUpdate.class, OnCreate.class})
    private Integer evaluation;

    private String comment;

    @NotNull(groups = {OnUpdate.class, OnCreate.class})
    private Initiator initiator;
}
