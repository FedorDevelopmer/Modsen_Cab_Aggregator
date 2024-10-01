package com.modsen.software.driver.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.dto.RatingEvaluationResponseTO;
import com.modsen.software.driver.mapper.DriverMapper;
import com.modsen.software.driver.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DriverKafkaConsumer {

    @Autowired
    private DriverService service;

    @Autowired
    private DriverKafkaProducer producer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DriverMapper driverMapper;

    @KafkaListener(topics = "rating-driver", groupId = "rating-evaluation")
    public void listen(String message) {
        try {
            RatingEvaluationResponseTO ratingEvaluation = objectMapper.readValue(message, RatingEvaluationResponseTO.class);
            DriverResponseTO driver = service.findDriverById(ratingEvaluation.getId());
            driver.setRating(ratingEvaluation.getMeanEvaluation());
            service.updateDriverByKafka(ratingEvaluation);
            System.out.println("Driver Rating Updated");
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
    }
}
