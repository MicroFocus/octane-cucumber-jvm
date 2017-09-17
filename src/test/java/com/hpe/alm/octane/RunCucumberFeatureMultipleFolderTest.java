package com.hpe.alm.octane;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(plugin = {"junit:cuc_report.xml"}, features = {"src\\test\\resources\\com\\hpe\\alm\\octane\\F1","src\\test\\resources\\com\\hpe\\alm\\octane\\F2"})
public class RunCucumberFeatureMultipleFolderTest {

}