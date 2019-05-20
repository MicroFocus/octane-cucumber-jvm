package com.hpe.alm.octane;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "com.hpe.alm.octane.OctaneGherkinFormatter:gherkin-results\\RunCucumberFeatureMultipleFolderTest.xml", features = {"src\\test\\resources\\com\\hpe\\alm\\octane\\F1","src\\test\\resources\\com\\hpe\\alm\\octane\\F2"})
public class RunCucumberFeatureMultipleFolderTest {

}