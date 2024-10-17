package com.modsen.software.driver.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.Set;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "drivers")
public class Driver implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "driver_id")
    private Long id;

    @Column(name = "driver_name")
    private String name;

    @Column(name = "driver_surname")
    private String surname;

    @Column(name = "driver_email")
    private String email;

    @Column(name = "driver_phone")
    private String phoneNumber;

    @Column(name = "driver_rating")
    private BigDecimal rating;

    @Column(name = "driver_gender")
    private Gender gender;

    @Column(name = "driver_birth_date")
    private Date birthDate;

    @Column(name = "driver_rating_last_update")
    private LocalDateTime ratingUpdateTimestamp;

    @Column(name = "driver_remove_status")
    private RemoveStatus removeStatus;

    @OneToMany(mappedBy = "driver", cascade = CascadeType.PERSIST, fetch = FetchType.LAZY)
    @JsonManagedReference
    private Set<Car> cars;
}
