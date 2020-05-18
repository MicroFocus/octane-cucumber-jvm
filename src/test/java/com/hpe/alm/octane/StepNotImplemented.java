package com.hpe.alm.octane;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "com.hpe.alm.octane.OctaneGherkinFormatter:gherkin-results/StepNotImplemented.xml", features = "src/test/resources/scenarios/stepNotImplemented.feature")
public class StepNotImplemented {
}
