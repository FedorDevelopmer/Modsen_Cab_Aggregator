package com.modsen.software.passenger.mapper;

import com.modsen.software.passenger.dto.PassengerRequestTO;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.entity.Passenger;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-09-08T19:07:31+0300",
    comments = "version: 1.6.0, compiler: javac, environment: Java 20.0.2 (Oracle Corporation)"
)
@Component
public class PassengerMapperImpl implements PassengerMapper {

    @Override
    public PassengerResponseTO passengerToResponse(Passenger driver) {
        if ( driver == null ) {
            return null;
        }

        PassengerResponseTO passengerResponseTO = new PassengerResponseTO();

        passengerResponseTO.setId( driver.getId() );
        passengerResponseTO.setName( driver.getName() );
        passengerResponseTO.setEmail( driver.getEmail() );
        passengerResponseTO.setPhoneNumber( driver.getPhoneNumber() );
        passengerResponseTO.setGender( driver.getGender() );
        passengerResponseTO.setRemoveStatus( driver.getRemoveStatus() );

        return passengerResponseTO;
    }

    @Override
    public Passenger responseToPassenger(PassengerResponseTO driverResponseTo) {
        if ( driverResponseTo == null ) {
            return null;
        }

        Passenger passenger = new Passenger();

        passenger.setId( driverResponseTo.getId() );
        passenger.setName( driverResponseTo.getName() );
        passenger.setEmail( driverResponseTo.getEmail() );
        passenger.setPhoneNumber( driverResponseTo.getPhoneNumber() );
        passenger.setGender( driverResponseTo.getGender() );
        passenger.setRemoveStatus( driverResponseTo.getRemoveStatus() );

        return passenger;
    }

    @Override
    public PassengerRequestTO passengerToRequest(Passenger driver) {
        if ( driver == null ) {
            return null;
        }

        PassengerRequestTO passengerRequestTO = new PassengerRequestTO();

        passengerRequestTO.setId( driver.getId() );
        passengerRequestTO.setName( driver.getName() );
        passengerRequestTO.setEmail( driver.getEmail() );
        passengerRequestTO.setPhoneNumber( driver.getPhoneNumber() );
        passengerRequestTO.setGender( driver.getGender() );
        passengerRequestTO.setRemoveStatus( driver.getRemoveStatus() );

        return passengerRequestTO;
    }

    @Override
    public Passenger requestToPassenger(PassengerRequestTO driverRequestTo) {
        if ( driverRequestTo == null ) {
            return null;
        }

        Passenger passenger = new Passenger();

        passenger.setId( driverRequestTo.getId() );
        passenger.setName( driverRequestTo.getName() );
        passenger.setEmail( driverRequestTo.getEmail() );
        passenger.setPhoneNumber( driverRequestTo.getPhoneNumber() );
        passenger.setGender( driverRequestTo.getGender() );
        passenger.setRemoveStatus( driverRequestTo.getRemoveStatus() );

        return passenger;
    }
}
