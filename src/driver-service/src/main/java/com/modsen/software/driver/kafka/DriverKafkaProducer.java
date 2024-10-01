package com.modsen.software.driver.kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class DriverKafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private final String TOPIC = "driver-rating";

    public void sendMessage(String message) {
        kafkaTemplate.send(TOPIC, message);
        System.out.println("Sent From Driver to Rating Service: " + message);
    }
}
