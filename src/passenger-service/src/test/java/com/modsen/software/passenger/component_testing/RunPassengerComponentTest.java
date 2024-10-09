package com.modsen.software.passenger.component_testing;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(features = "src/test/resources/passenger_service.feature")
public class RunPassengerComponentTest {
}
