package com.modsen.software.passenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {
        "com.modsen.software.passenger"})
@EnableScheduling
public class PassengerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(PassengerServiceApplication.class, args);
    }
}