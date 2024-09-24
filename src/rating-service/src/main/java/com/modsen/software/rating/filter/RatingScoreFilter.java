package com.modsen.software.rating.filter;

import com.modsen.software.rating.entity.enumeration.Initiator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RatingScoreFilter {

    private Long driverId = null;

    private Long passengerId = null;

    private Integer evaluation = null;

    private Integer evaluationHigher = null;

    private Integer evaluationLower = null;

    private Initiator initiator = null;
}
