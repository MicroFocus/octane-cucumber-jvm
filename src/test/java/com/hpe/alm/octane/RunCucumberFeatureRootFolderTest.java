package com.hpe.alm.octane;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = {"com.hpe.alm.octane.OctaneGherkinFormatter:gherkin-results/RunCucumberFeatureRootFolderTest.xml", "junit:cuc_report.xml"}, features = "src/test/resources/com")
public class RunCucumberFeatureRootFolderTest {

}