package com.modsen.software.passenger.filter;

import com.modsen.software.passenger.entity.enumeration.Gender;
import com.modsen.software.passenger.entity.enumeration.RemoveStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PassengerFilter {

    private String name = null;

    private String email = null;

    private String phoneNumber = null;

    private Gender gender = null;

    private RemoveStatus removeStatus = null;
}
