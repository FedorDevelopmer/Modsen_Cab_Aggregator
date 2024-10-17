package com.modsen.software.passenger.contract_producer;

import com.modsen.software.passenger.PassengerServiceApplication;
import com.modsen.software.passenger.controller.PassengerController;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

@RunWith(SpringRunner.class)
@AutoConfigureMessageVerifier
@SpringBootTest(classes = PassengerServiceApplication.class)
public class BaseTestClass {

    @Autowired
    private static PassengerController passengerController;

    @BeforeEach
    void setUp() {
        StandaloneMockMvcBuilder standaloneMockMvcBuilder
                = MockMvcBuilders.standaloneSetup(passengerController);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }
}