package com.modsen.software.passenger.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.dto.RatingEvaluationResponseTO;
import com.modsen.software.passenger.mapper.PassengerMapper;
import com.modsen.software.passenger.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PassengerKafkaConsumer {

    @Autowired
    private PassengerService service;

    @Autowired
    private PassengerKafkaProducer producer;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PassengerMapper passengerMapper;

    @KafkaListener(topics = "rating-passenger", groupId = "rating-evaluation")
    public void listen(String message) {
        try {
            RatingEvaluationResponseTO ratingEvaluation = objectMapper.readValue(message, RatingEvaluationResponseTO.class);
            PassengerResponseTO passenger = service.findPassengerById(ratingEvaluation.getId());
            passenger.setRating(ratingEvaluation.getMeanEvaluation());
            service.updatePassengerByKafka(ratingEvaluation);
            System.out.println("Passenger Rating Updated");
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
    }
}
