package com.modsen.software.rating.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.rating.dto.DriverResponseTO;
import com.modsen.software.rating.dto.PassengerResponseTO;
import com.modsen.software.rating.dto.RatingEvaluationResponseTO;
import com.modsen.software.rating.entity.enumeration.Initiator;
import com.modsen.software.rating.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class RatingKafkaConsumer {

    @Autowired
    private RatingService service;

    @Autowired
    private RatingKafkaProducer producer;

    @Autowired
    private ObjectMapper mapper;

    @KafkaListener(topics = "driver-rating", groupId = "rating-evaluation")
    public void listenDriver(String message){
        try {
            DriverResponseTO driver = mapper.readValue(message, DriverResponseTO.class);
            RatingEvaluationResponseTO ratingEvaluation = service.evaluateMeanRatingById(driver.getId(), Initiator.DRIVER, PageRequest.of(0,50));
            String jsonRatingObject = mapper.writeValueAsString(ratingEvaluation);
            producer.sendMessageDriver(jsonRatingObject);
        } catch (JsonProcessingException e){
            System.out.println(e.getMessage());
        }
    }

    @KafkaListener(topics = "passenger-rating", groupId = "rating-evaluation")
    public void listenPassenger(String message){
        try {
            PassengerResponseTO passenger = mapper.readValue(message, PassengerResponseTO.class);
            RatingEvaluationResponseTO ratingEvaluation = service.evaluateMeanRatingById(passenger.getId(), Initiator.PASSENGER, PageRequest.of(0,50));
            String jsonRatingObject = mapper.writeValueAsString(ratingEvaluation);
            producer.sendMessagePassenger(jsonRatingObject);
        } catch (JsonProcessingException e){
            System.out.println(e.getMessage());
        }
    }
}
