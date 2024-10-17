package com.modsen.software.driver.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import jakarta.persistence.*;
import java.io.Serializable;
import java.sql.Date;
import lombok.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@Table(name = "cars")
public class Car implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "car_id")
    private Long id;

    @Column(name = "car_driver")
    private Long driverId;

    @Column(name = "car_color")
    private Color color;

    @Column(name = "car_brand")
    private String brand;

    @Column(name = "car_reg_number")
    private String registrationNumber;

    @Column(name = "car_inspection_date")
    private Date inspectionDate;

    @Column(name = "car_inspection_duration_month")
    private Integer inspectionDurationMonth;

    @Column(name = "car_remove_status")
    private RemoveStatus removeStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    @JsonBackReference
    private Driver driver;
}
