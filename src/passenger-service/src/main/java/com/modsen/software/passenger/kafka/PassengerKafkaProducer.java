package com.modsen.software.passenger.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PassengerKafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String TOPIC = "passenger-rating";

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("Sent From Passenger to Rating Service: " + message);
    }
}
