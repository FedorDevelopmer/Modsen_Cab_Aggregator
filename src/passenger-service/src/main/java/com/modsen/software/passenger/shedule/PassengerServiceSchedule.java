package com.modsen.software.passenger.shedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.passenger.dto.PassengerResponseTO;
import com.modsen.software.passenger.filter.PassengerFilter;
import com.modsen.software.passenger.kafka.PassengerKafkaProducer;
import com.modsen.software.passenger.service.PassengerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PassengerServiceSchedule {

    @Autowired
    PassengerService service;

    @Autowired
    private PassengerKafkaProducer producer;

    @Autowired
    private ObjectMapper mapper;

    @Scheduled(fixedRate = 60000)
    public void performPassengerRatingUpdate() throws JsonProcessingException {
        PassengerFilter filter = new PassengerFilter();
        int pageIndex = 0;
        boolean isLastPage = false;

        while (!isLastPage) {
            Pageable pageable = PageRequest.of(pageIndex, 10);
            Page<PassengerResponseTO> passengers = service.getAllPassengers(filter, pageable);
            for (PassengerResponseTO driverResponseTO : passengers.getContent()) {
                String jsonDriverObject = mapper.writeValueAsString(driverResponseTO);
                producer.sendMessage(jsonDriverObject);
            }
            pageIndex++;
            isLastPage = passengers.isLast();
        }
    }
}
