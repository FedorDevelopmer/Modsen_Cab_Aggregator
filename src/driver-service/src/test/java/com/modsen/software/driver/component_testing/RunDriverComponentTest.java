package com.modsen.software.driver.component_testing;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;

@RunWith(Cucumber.class)
@CucumberOptions(features = {"src/test/resources/driver_service.feature",
        "src/test/resources/car_service.feature"})
@ActiveProfiles("test")
public class RunDriverComponentTest {
}
