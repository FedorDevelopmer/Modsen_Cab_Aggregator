package com.modsen.software.driver.contract_producer;

import com.modsen.software.driver.DriverServiceApplication;
import com.modsen.software.driver.controller.DriverController;
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
@SpringBootTest(classes = DriverServiceApplication.class)
public class BaseTestClass {

    @Autowired
    private static DriverController driverController;

    @BeforeEach
    void setUp() {
        StandaloneMockMvcBuilder standaloneMockMvcBuilder
                = MockMvcBuilders.standaloneSetup(driverController);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }
}
