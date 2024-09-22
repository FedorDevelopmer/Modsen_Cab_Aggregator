package com.modsen.software.passenger.entity;

import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    @Column(name = "passenger_remove_status")
    private RemoveStatus removeStatus;
}
