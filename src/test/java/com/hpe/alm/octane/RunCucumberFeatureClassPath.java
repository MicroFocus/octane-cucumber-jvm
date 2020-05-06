package com.hpe.alm.octane;

import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin = "com.hpe.alm.octane.OctaneGherkinFormatter:gherkin-results/RunCucumberFeatureClassPath.xml", features = {"classpath:com/hpe/alm/octane/F1/test1.feature","classpath:com/hpe/alm/octane/F2/test2.feature"})
public class RunCucumberFeatureClassPath {

}