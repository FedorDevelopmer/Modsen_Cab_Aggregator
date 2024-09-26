package com.modsen.software.ride.specification;

import com.modsen.software.ride.entity.Ride;
import com.modsen.software.ride.entity.enumeration.RideStatus;
import org.springframework.data.jpa.domain.Specification;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class RideSpecification {

    public static Specification<Ride> hasDriverId(Long driverId) {
        return (root, query, criteriaBuilder) -> driverId == null ? null : criteriaBuilder.equal(root.get("driverId"), driverId);
    }

    public static Specification<Ride> hasPassengerId(Long passengerId) {
        return (root, query, criteriaBuilder) -> passengerId == null ? null : criteriaBuilder.equal(root.get("passengerId"), passengerId);
    }

    public static Specification<Ride> hasDepartureAddress(String departureAddress) {
        return (root, query, criteriaBuilder) -> departureAddress == null ? null : criteriaBuilder.like(root.get("departureAddress"), "%" + departureAddress + "%");
    }

    public static Specification<Ride> hasDestinationAddress(String destinationAddress) {
        return (root, query, criteriaBuilder) -> destinationAddress == null ? null : criteriaBuilder.like(root.get("destinationAddress"), "%" + destinationAddress + "%");
    }

    public static Specification<Ride> hasRideOrderTime(LocalDateTime time) {
        return (root, query, criteriaBuilder) -> time == null ? null : criteriaBuilder.lessThan(root.get("rideOrderTime"), time);
    }

    public static Specification<Ride> hasRideOrderTimeLater(LocalDateTime after) {
        return (root, query, criteriaBuilder) -> after == null ? null : criteriaBuilder.greaterThan(root.get("rideOrderTimeAndLater"), after);
    }

    public static Specification<Ride> hasRideOrderTimeEarlier(LocalDateTime before) {
        return (root, query, criteriaBuilder) -> before == null ? null : criteriaBuilder.lessThan(root.get("rideOrderTimeAndEarlier"), before);
    }

    public static Specification<Ride> hasRideStatus(RideStatus rideStatus) {
        return (root, query, criteriaBuilder) -> rideStatus == null ? null : criteriaBuilder.equal(root.get("rideStatus"), rideStatus);
    }

    public static Specification<Ride> hasRidePrice(BigDecimal price) {
        return (root, query, criteriaBuilder) -> price == null ? null : criteriaBuilder.equal(root.get("ridePrice"), price);
    }

    public static Specification<Ride> hasRidePriceLowerThan(BigDecimal price) {
        return (root, query, criteriaBuilder) -> price == null ? null : criteriaBuilder.lessThan(root.get("ridePriceAndLower"), price);
    }

    public static Specification<Ride> hasRidePriceHigherThan(BigDecimal price) {
        return (root, query, criteriaBuilder) -> price == null ? null : criteriaBuilder.greaterThan(root.get("ridePriceAndHigher"), price);
    }
}

