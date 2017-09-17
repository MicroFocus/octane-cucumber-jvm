package com.hpe.alm.octane;

import com.hpe.alm.octane.OctaneCucumber;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(features = "src\\test\\resources\\scenarios\\stepNotImplemented.feature")
public class StepNotImplemented {
}
