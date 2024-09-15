package com.modsen.software.driver.entity;

import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    @Column(name = "driver_remove_status")
    private RemoveStatus removeStatus;

    @OneToMany(mappedBy = "driver", fetch = FetchType.LAZY)
    private Set<Car> cars;
}
