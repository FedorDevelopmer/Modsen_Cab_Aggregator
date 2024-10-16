package com.modsen.software.rating.error_decoder;

import com.modsen.software.rating.exception.PassengerNotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.http.HttpStatus;

public class FeignPassengerErrorDecoder implements ErrorDecoder {

    private final ErrorDecoder defaultDecoder = new ErrorDecoder.Default();

    @Override
    public Exception decode(String methodKey, Response response) {
        if(response.status() == HttpStatus.NOT_FOUND.value()){
            throw new PassengerNotFoundException();
        }
        return defaultDecoder.decode(methodKey,response);
    }
}
