package com.modsen.software.passenger.repository;

import com.modsen.software.passenger.entity.Passenger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PassengerRepository extends JpaRepository<Passenger, Long>, JpaSpecificationExecutor<Passenger> {
    Optional<Passenger> getByEmail(String email);

    Optional<Passenger> getByPhoneNumber(String phoneNumber);
}