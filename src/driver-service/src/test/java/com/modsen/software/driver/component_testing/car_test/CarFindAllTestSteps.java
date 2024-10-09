package com.modsen.software.driver.component_testing.car_test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.modsen.software.driver.dto.CarRequestTO;
import com.modsen.software.driver.entity.Car;
import com.modsen.software.driver.entity.Driver;
import com.modsen.software.driver.entity.enumeration.Color;
import com.modsen.software.driver.entity.enumeration.Gender;
import com.modsen.software.driver.entity.enumeration.RemoveStatus;
import com.modsen.software.driver.mapper.CarMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

public class CarFindAllTestSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CarMapper carMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private MvcResult result;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @When("I request all cars from database through service")
    public void requestAllCars() throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + CarTestLoader.CARS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestCars();
        result = mockMvc.perform(get(CarTestLoader.URI).contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @Then("all cars request complete with code {int} \\(OK)")
    public void allCarsCodeIsOk(Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @Then("all {string} cars request complete with code {int} \\(OK)")
    public void allFilteredByBrandCarsResponseCodeIsOk(String brand, Integer code) {
        Assertions.assertEquals(code, result.getResponse().getStatus());
    }

    @And("first cars page should be returned")
    public void responseContainCarsPage() throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer driversCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(driversCount > 0);
    }

    @When("I request all cars with brand {string} from database through service")
    public void requestAllCarFilteredByBrand(String brand) throws Exception {
        jdbcTemplate.execute("TRUNCATE TABLE " + CarTestLoader.CARS_TABLE_NAME + " RESTART IDENTITY CASCADE");
        saveTestCars();
        result = mockMvc.perform(get(CarTestLoader.URI)
                .param("brand", brand)
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }

    @And("first page of filtered with brand {string} cars should be returned")
    public void responseContainFilteredByBrand(String brand) throws Exception {
        String contentString = result.getResponse().getContentAsString();
        Integer driversCount = JsonPath.read(contentString, "$.content.length()");
        Integer pageNumber = JsonPath.read(contentString, "$.number");
        List<LinkedHashMap<String, Object>> carsMaps = JsonPath.parse(contentString).read("$.content", List.class);
        for (LinkedHashMap<String, Object> driverMap : carsMaps) {
            Assertions.assertEquals(brand, driverMap.get("brand"));
        }
        Assertions.assertEquals(0, pageNumber);
        Assertions.assertTrue(driversCount > 0);
    }

    private void saveTestCars() throws Exception {
        Car testCar = Car.builder()
                .driverId(1L)
                .color(Color.GRAY)
                .brand("Honda")
                .registrationNumber("7 TAX 4332")
                .inspectionDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .inspectionDurationMonth(24)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();

        Car testCarSecond = Car.builder()
                .driverId(1L)
                .color(Color.BLACK)
                .brand("Bugatti")
                .registrationNumber("7 TAX 3242")
                .inspectionDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .inspectionDurationMonth(6)
                .removeStatus(RemoveStatus.ACTIVE)
                .build();

        Driver testDriver = Driver.builder()
                .name("John")
                .surname("Conor")
                .email("john.c@gmail.com")
                .rating(BigDecimal.valueOf(5))
                .birthDate(new Date(System.currentTimeMillis() - CarTestLoader.MONTH_DURATION))
                .phoneNumber("+323-322-243")
                .gender(Gender.MALE)
                .removeStatus(RemoveStatus.ACTIVE)
                .ratingUpdateTimestamp(LocalDateTime.now())
                .cars(new HashSet<>())
                .build();

        mockMvc.perform(post("/api/v1/drivers")
                .content(objectMapper.writeValueAsString(testDriver))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();

        saveCar(testCar);
        saveCar(testCarSecond);
    }

    private void saveCar(Car car) throws Exception {
        CarRequestTO carRequest = carMapper.carToRequest(car);
        mockMvc.perform(post(CarTestLoader.URI)
                .content(objectMapper.writeValueAsString(carRequest))
                .contentType(MediaType.APPLICATION_JSON)).andReturn();
    }
}
