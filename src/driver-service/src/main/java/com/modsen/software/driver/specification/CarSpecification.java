package com.modsen.software.driver.specification;

import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import java.sql.Date;
import org.springframework.data.jpa.domain.Specification;

public class CarSpecification {

    public static Specification<Car> hasColor(Color color) {
        return (root, query, criteriaBuilder) -> color == null ? null : criteriaBuilder.equal(root.get("color"), color);
    }

    public static Specification<Car> hasBrand(String brand) {
        return (root, query, criteriaBuilder) -> brand == null ? null : criteriaBuilder.equal(root.get("brand"), brand);
    }

    public static Specification<Car> hasRegistrationNumber(String registrationNumber) {
        return (root, query, criteriaBuilder) -> registrationNumber == null ? null : criteriaBuilder.equal(root.get("registrationNumber"), registrationNumber);
    }

    public static Specification<Car> hasInspectionDateEarlier(Date inspectionDate) {
        return (root, query, criteriaBuilder) -> inspectionDate == null ? null : criteriaBuilder.lessThan(root.get("inspectionDate"), inspectionDate);
    }

    public static Specification<Car> hasInspectionDateLater(Date inspectionDate) {
        return (root, query, criteriaBuilder) -> inspectionDate == null ? null : criteriaBuilder.greaterThan(root.get("inspectionDate"), inspectionDate);
    }

    public static Specification<Car> hasInspectionDate(Date inspectionDate) {
        return (root, query, criteriaBuilder) -> inspectionDate == null ? null : criteriaBuilder.equal(root.get("inspectionDate"), inspectionDate);
    }

    public static Specification<Car> hasInspectionDurationMonth(Integer inspectionDurationMonth) {
        return (root, query, criteriaBuilder) -> inspectionDurationMonth == null ? null : criteriaBuilder.equal(root.get("inspectionDurationMonth"), inspectionDurationMonth);
    }

    public static Specification<Car> hasRemoveStatus(RemoveStatus removeStatus) {
        return (root, query, criteriaBuilder) -> removeStatus == null ? null : criteriaBuilder.equal(root.get("removeStatus"), removeStatus);
    }
}
