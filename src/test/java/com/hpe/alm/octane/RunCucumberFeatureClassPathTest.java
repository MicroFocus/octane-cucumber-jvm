package com.hpe.alm.octane;

import com.hpe.alm.octane.OctaneCucumber;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(plugin = {"junit:cuc_report.xml"},features = {"classpath:F1\\test1.feature","classpath:F2\\test2.feature"})
public class RunCucumberFeatureClassPathTest {

}