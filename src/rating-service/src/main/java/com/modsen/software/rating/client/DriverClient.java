package com.modsen.software.rating.client;

import com.modsen.software.rating.dto.DriverResponseTO;
import feign.Param;
import feign.RequestLine;

public interface DriverClient {
    @RequestLine("GET /{id}")
    DriverResponseTO getDriver(@Param("id") Long id);
}
