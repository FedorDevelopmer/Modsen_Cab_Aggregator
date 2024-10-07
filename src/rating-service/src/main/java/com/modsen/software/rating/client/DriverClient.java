package com.modsen.software.rating.client;

import com.modsen.software.rating.dto.DriverResponseTO;
import com.modsen.software.rating.error_decoder.FeignDriverErrorDecoder;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Component
@FeignClient(value = "driverClient", url = "http://localhost:8080/api/v1/drivers", configuration = FeignDriverErrorDecoder.class)
public interface DriverClient {
    @RequestMapping(method = RequestMethod.GET, path = "/{id}")
    DriverResponseTO getDriver(@PathVariable("id") Long id);
}
