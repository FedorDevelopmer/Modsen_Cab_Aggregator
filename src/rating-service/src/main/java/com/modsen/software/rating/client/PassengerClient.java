package com.modsen.software.rating.client;

import com.modsen.software.rating.dto.PassengerResponseTO;
import feign.Param;
import feign.RequestLine;

public interface PassengerClient {
    @RequestLine("GET /{id}")
    PassengerResponseTO getPassenger(@Param("id") Long id);
}
