package com.modsen.software.driver.specification;

import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import org.springframework.data.jpa.domain.Specification;
import java.sql.Date;

public class DriverSpecification {
    public static Specification<Driver> hasName(String name) {
        return (root, query, criteriaBuilder) -> name == null ? null : criteriaBuilder.equal(root.get("name"), name);
    }

    public static Specification<Driver> hasSurname(String surname) {
        return (root, query, criteriaBuilder) -> surname == null ? null : criteriaBuilder.equal(root.get("surname"), surname);
    }

    public static Specification<Driver> hasEmail(String email) {
        return (root, query, criteriaBuilder) -> email == null ? null : criteriaBuilder.equal(root.get("email"), email);
    }

    public static Specification<Driver> hasPhone(String phoneNumber) {
        return (root, query, criteriaBuilder) -> phoneNumber == null ? null : criteriaBuilder.equal(root.get("phoneNumber"), phoneNumber);
    }

    public static Specification<Driver> hasGender(Gender gender) {
        return (root, query, criteriaBuilder) -> gender == null ? null : criteriaBuilder.equal(root.get("gender"), gender);
    }

    public static Specification<Driver> hasBirthDateEarlier(Date birthDate) {
        return (root, query, criteriaBuilder) -> birthDate == null ? null : criteriaBuilder.lessThan(root.get("birthDate"), birthDate);
    }

    public static Specification<Driver> hasBirthDate(Date birthDate) {
        return (root, query, criteriaBuilder) -> birthDate == null ? null : criteriaBuilder.equal(root.get("birthDate"), birthDate);
    }

    public static Specification<Driver> hasBirthDateLater(Date birthDate) {
        return (root, query, criteriaBuilder) -> birthDate == null ? null : criteriaBuilder.greaterThan(root.get("birthDate"), birthDate);
    }

    public static Specification<Driver> hasRemoveStatus(RemoveStatus removeStatus) {
        return (root, query, criteriaBuilder) -> removeStatus == null ? null : criteriaBuilder.equal(root.get("removeStatus"), removeStatus);
    }
}
