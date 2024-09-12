package com.modsen.software.rating.repository;


import com.modsen.software.rating.entity.RatingScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RatingRepository extends JpaRepository<RatingScore, Long> {

}