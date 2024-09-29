package com.modsen.software.passenger.entity;

import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "passengers")
public class Passenger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "passenger_id")
    private Long id;

    @Column(name = "passenger_name")
    private String name;

    @Column(name = "passenger_email")
    private String email;

    @Column(name = "passenger_phone")
    private String phoneNumber;

    @Column(name = "passenger_rating")
    private BigDecimal rating;

    @Column(name = "passenger_gender")
    private Gender gender;

    @Column(name = "passenger_rating_last_update")
    private LocalDateTime ratingUpdateTimestamp;

    @Column(name = "passenger_remove_status")
    private RemoveStatus removeStatus;
}
