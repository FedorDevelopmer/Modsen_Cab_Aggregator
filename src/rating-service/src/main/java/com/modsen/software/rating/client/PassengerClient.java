package com.modsen.software.rating.client;

import com.modsen.software.rating.dto.PassengerResponseTO;
import com.modsen.software.rating.error_decoder.FeignPassengerErrorDecoder;
import feign.Param;
import feign.RequestLine;
import org.springframework.cloud.openfeign.CollectionFormat;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
@FeignClient(value = "passengerClient", url = "http://localhost:8081/api/v1/passengers", configuration = FeignPassengerErrorDecoder.class)
public interface PassengerClient {
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    PassengerResponseTO getPassenger(@PathVariable("id") Long id);
}
