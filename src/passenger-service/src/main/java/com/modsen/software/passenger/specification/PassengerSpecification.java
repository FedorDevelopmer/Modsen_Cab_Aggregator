package com.modsen.software.passenger.specification;

import com.modsen.software.passenger.entity.Passenger;
import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import org.springframework.data.jpa.domain.Specification;

public class PassengerSpecification {

    public static Specification<Passenger> hasName(String name){
        return (root, query, builder) -> name == null ? null : builder.equal(root.get("name"),name);
    }

    public static Specification<Passenger> hasEmail(String email){
        return (root, query, builder) -> email == null ? null : builder.equal(root.get("name"),email);
    }

    public static Specification<Passenger> hasPhone(String phoneNumber){
        return (root, query, builder) -> phoneNumber == null ? null : builder.equal(root.get("phoneNumber"),phoneNumber);
    }

    public static Specification<Passenger> hasGender(Gender gender){
        return (root, query, builder) -> gender == null ? null : builder.equal(root.get("gender"),gender);
    }

    public static Specification<Passenger> hasRemoveStatus(RemoveStatus removeStatus){
        return (root, query, builder) -> removeStatus == null ? null : builder.equal(root.get("removeStatus"),removeStatus);
    }
}
