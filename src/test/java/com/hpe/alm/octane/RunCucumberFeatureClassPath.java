package com.hpe.alm.octane;

import cucumber.api.CucumberOptions;

@CucumberOptions(plugin = {"junit:cuc_report.xml"},features = {"classpath:com\\hpe\\alm\\octane\\F1\\test1.feature","classpath:com\\hpe\\alm\\octane\\F2\\test2.feature"})
public class RunCucumberFeatureClassPath {

}