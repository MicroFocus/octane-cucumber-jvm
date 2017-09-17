package com.hpe.alm.octane;

import cucumber.api.CucumberOptions;

@CucumberOptions(plugin = {"junit:cuc_report.xml"}, features = "src\\test\\resources\\com\\hpe\\alm\\octane\\F1\\test1.feature")
public class RunCucumberFeatureFile {

}