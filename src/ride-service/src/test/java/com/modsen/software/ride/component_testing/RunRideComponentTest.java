package com.modsen.software.ride.component_testing;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/ride_service.feature")
@SpringBootTest
public class RunRideComponentTest {
}
