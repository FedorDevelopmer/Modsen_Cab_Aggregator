package com.modsen.software.rating.dto;

import com.modsen.software.rating.entity.enumeration.Initiator;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RatingScoreResponseTO {

    private Long id;

    private Long driverId;

    private Long passengerId;

    private Integer evaluation;

    private String comment;

    private Initiator initiator;
}
