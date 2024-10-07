package com.modsen.software.driver.shedule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.modsen.software.driver.dto.DriverResponseTO;
import com.modsen.software.driver.filter.DriverFilter;
import com.modsen.software.driver.kafka.DriverKafkaProducer;
import com.modsen.software.driver.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class DriverServiceSchedule {

    @Autowired
    DriverService service;

    @Autowired
    private DriverKafkaProducer producer;

    @Autowired
    private ObjectMapper mapper;

    @Scheduled(fixedRate = 60000)
    public void performDriverRatingUpdate() throws JsonProcessingException {
        DriverFilter filter = new DriverFilter();
        int pageIndex = 0;
        boolean isLastPage = false;

        while (!isLastPage) {
            Pageable pageable = PageRequest.of(pageIndex, 10);
            Page<DriverResponseTO> drivers = service.getAllDrivers(filter, pageable);
            for (DriverResponseTO driverResponseTO : drivers.getContent()) {
                String jsonDriverObject = mapper.writeValueAsString(driverResponseTO);
                producer.sendMessage(jsonDriverObject);
            }
            pageIndex++;
            isLastPage = drivers.isLast();
        }
    }
}
