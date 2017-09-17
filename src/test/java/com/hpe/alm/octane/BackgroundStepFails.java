package com.hpe.alm.octane;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(features = "src\\test\\resources\\scenarios\\backgroundStepFails.feature")
public class BackgroundStepFails {
}
