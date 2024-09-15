package com.modsen.software.rating.specification;

import com.modsen.software.rating.entity.RatingScore;
import com.modsen.software.rating.entity.enumeration.Initiator;
import org.springframework.data.jpa.domain.Specification;

public class RatingScoreSpecification {
    public static Specification<RatingScore> hasPassengerId(Long passengerId){
        return ((root, query, criteriaBuilder) -> passengerId == null ? null : criteriaBuilder.equal(root.get("passengerId"), passengerId));
    }

    public static Specification<RatingScore> hasDriverId(Long driverId){
        return ((root, query, criteriaBuilder) -> driverId == null ? null : criteriaBuilder.equal(root.get("driverId"), driverId));
    }

    public static Specification<RatingScore> hasEvaluation(Integer evaluation){
        return ((root, query, criteriaBuilder) -> evaluation == null ? null : criteriaBuilder.equal(root.get("evaluation"), evaluation));
    }

    public static Specification<RatingScore> hasEvaluationHigher(Integer evaluation){
        return ((root, query, criteriaBuilder) -> evaluation == null ? null : criteriaBuilder.greaterThan(root.get("evaluation"), evaluation));
    }

    public static Specification<RatingScore> hasEvaluationLower(Integer evaluation){
        return ((root, query, criteriaBuilder) -> evaluation == null ? null : criteriaBuilder.lessThan(root.get("evaluation"), evaluation));
    }

    public static Specification<RatingScore> hasInitiator(Initiator initiator){
        return ((root, query, criteriaBuilder) -> initiator == null ? null : criteriaBuilder.equal(root.get("initiator"), initiator));
    }
}
