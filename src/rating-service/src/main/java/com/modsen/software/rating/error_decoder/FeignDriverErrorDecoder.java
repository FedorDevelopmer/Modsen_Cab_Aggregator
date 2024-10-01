package com.modsen.software.rating.error_decoder;

import com.modsen.software.rating.exception.DriverNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

public class FeignDriverErrorDecoder implements ErrorDecoder {
    private final ErrorDecoder defaultDecoder = new Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if(response.status() == HttpStatus.NOT_FOUND.value()){
            throw new DriverNotFoundException();
        }
        return defaultDecoder.decode(methodKey,response);
    }
}
