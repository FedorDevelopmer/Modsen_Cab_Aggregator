package com.modsen.software.rating.contract_consumer;

import com.modsen.software.rating.entity.enumeration.Gender;
import com.modsen.software.rating.entity.enumeration.RemoveStatus;
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
        ids = {"com.modsen.software:driver-service:+:stubs:8080"}, mappingsOutputFolder = "/output/mappings")
public class RatingDriverContractConsumerTest {

    private final String URL = "http://localhost:8080/api/v1/drivers/{id}";

    @Test
    public void Given_ExistingDriverWithId_When_GetByIdRequest_Then_ReturnDriverEntity() {

        Response response = RestAssured.given()
                .when()
                .header("Content-Type", "application/json")
                .get(URL, 1).andReturn();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(200, response.getStatusCode());
        Assertions.assertEquals("John", response.jsonPath().getString("name"));
        Assertions.assertEquals("Doe", response.jsonPath().getString("surname"));
        Assertions.assertEquals("john.doe@mail.com", response.jsonPath().getString("email"));
        Assertions.assertEquals("+345-343-211", response.jsonPath().getString("phoneNumber"));
        Assertions.assertEquals(Gender.MALE.name(), response.jsonPath().getString("gender"));
        Assertions.assertEquals(RemoveStatus.ACTIVE.name(), response.jsonPath().getString("removeStatus"));
    }

    @Test
    public void Given_NotExistingDriverWithId_When_GetByIdRequest_Then_ReturnNotFoundResponse() {

        Response response = RestAssured.given()
                .when()
                .header("Content-Type", "application/json")
                .get(URL, 1001).andReturn();
        Assertions.assertNotNull(response);
        Assertions.assertEquals(404, response.getStatusCode());
        Assertions.assertEquals("Requested driver doesn't exist", response.jsonPath().getString("message"));
    }
}
