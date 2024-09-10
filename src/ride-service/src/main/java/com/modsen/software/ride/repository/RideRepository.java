package com.modsen.software.ride.repository;


import com.modsen.software.ride.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {

}