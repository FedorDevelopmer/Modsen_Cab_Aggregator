package com.modsen.software.rating.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RatingKafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String DRIVER_TOPIC = "rating-driver";

    private final String PASSENGER_TOPIC = "rating-passenger";

    public void sendMessageDriver(String message) throws JsonProcessingException  {
        kafkaTemplate.send(DRIVER_TOPIC, message);
        System.out.println("Sent by Rating to Driver Service: " + message);
    }

    public void sendMessagePassenger(String message) throws JsonProcessingException  {
        kafkaTemplate.send(PASSENGER_TOPIC, message);
        System.out.println("Sent by Rating to Passenger Service: " + message);
    }
}
