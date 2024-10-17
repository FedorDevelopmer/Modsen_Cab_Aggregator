package com.modsen.software.passenger.contract_consumer;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@AutoConfigureJsonTesters
@AutoConfigureStubRunner(stubsMode = StubRunnerProperties.StubsMode.LOCAL,
        ids = {"com.modsen.software:rating-service:+:stubs:8082"})
public class PassengerRatingContractConsumerTest {

    private final String URL = "http://localhost:8082/api/v1/scores/evaluate/{id}";

    @Test
    public void Given_ExistingPassengerWithId_When_MeanRatingEvaluationRequest_Then_ReturnEvaluationResponse() {

        Response response = RestAssured.given()
                .when()
                .header("Content-Type", "application/json")
                .param("initiator", "PASSENGER")
                .get(URL, 2).andReturn();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("2", response.jsonPath().getString("id"));
        Assertions.assertEquals("4.46", response.jsonPath().getString("meanEvaluation"));

    }
}
