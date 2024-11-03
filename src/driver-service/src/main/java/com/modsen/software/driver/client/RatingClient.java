package com.modsen.software.driver.client;

import com.modsen.software.driver.dto.RatingEvaluationResponseTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Component
@FeignClient(name = "rating-service-client")
public interface RatingClient {

    @RequestMapping(method = RequestMethod.GET, path = "/api/v1/scores/evaluate/{id}")
    RatingEvaluationResponseTO evaluateRating(@RequestParam String initiator, @PathVariable("id") Long id);
}
