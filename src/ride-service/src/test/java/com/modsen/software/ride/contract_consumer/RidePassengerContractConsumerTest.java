package com.modsen.software.ride.contract_consumer;

import com.modsen.software.ride.entity.enumeration.Gender;
import com.modsen.software.ride.entity.enumeration.RemoveStatus;
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
        ids = {"com.modsen.software:passenger-service:+:stubs:8081"})
public class RidePassengerContractConsumerTest {

    private final String URL = "http://localhost:8081/api/v1/passengers/{id}";

    @Test
    public void Given_ExistingPassengerWithId_When_GetByIdRequest_Then_ReturnPassengerEntity() {

        Response response = RestAssured.given()
                .when()
                .header("Content-Type", "application/json")
                .get(URL, 1).andReturn();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("Mike", response.jsonPath().getString("name"));
        Assertions.assertEquals("mike.w@mail.com", response.jsonPath().getString("email"));
        Assertions.assertEquals("+122-222-221", response.jsonPath().getString("phoneNumber"));
        Assertions.assertEquals(Gender.MALE.name(), response.jsonPath().getString("gender"));
        Assertions.assertEquals(RemoveStatus.ACTIVE.name(), response.jsonPath().getString("removeStatus"));
    }

    @Test
    public void Given_NotExistingPassengerWithId_When_GetByIdRequest_Then_ReturnNotFoundResponse() {

        Response response = RestAssured.given()
                .when()
                .header("Content-Type", "application/json")
                .get(URL, 101).andReturn();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals("Requested passenger doesn't exist", response.jsonPath().getString("message"));
    }
}

