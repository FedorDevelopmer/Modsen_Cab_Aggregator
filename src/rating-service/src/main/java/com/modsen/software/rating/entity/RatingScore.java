package com.modsen.software.rating.entity;

import com.modsen.software.rating.entity.enumeration.Initiator;
import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "rating_scores")
public class RatingScore {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rating_score_id")
    private Long id;

    @Column(name = "rating_score_driver_id")
    private Long driverId;

    @Column(name = "rating_score_passenger_id")
    private Long passengerId;

    @Column(name = "rating_score_evaluation")
    private Integer evaluation;

    @Column(name = "rating_score_comment")
    private String comment;

    @Column(name = "rating_score_initiator")
    private Initiator initiator;
}