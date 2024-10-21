package com.modsen.software.driver.repository;

import com.modsen.software.driver.entity.Driver;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long>, JpaSpecificationExecutor<Driver> {
    Optional<Driver> findByEmail(String email);

    Optional<Driver> findByPhoneNumber(String phone);
}