package com.hpe.alm.octane;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "com.hpe.alm.octane.OctaneGherkinFormatter:gherkin-results/RunMultipleRuleFeatureTest.xml", features = "src/test/resources/com/hpe/alm/octane/F1/rule.feature")
public class RunMultipleRuleFeatureTest {
}
