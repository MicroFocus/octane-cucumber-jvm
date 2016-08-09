# octane-cucumber-jvm
ALM Octane cucumber-jvm formatter 

This plugin enable uploading cucumber-jvm tests back into ALM Octane.

Please notice that the example below uses junit to run cucumber-jvm tests using the @RunWith attribute.
To use our formatter, we changed in the example below the @RunWith parameter from cucumber.class to OctaneCucumber.class 

Usage Example
==============
package feature.manualRunner;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;
import cucumber.api.junit.Cucumber;

@RunWith(OctaneCucumber.class)
@CucumberOptions(plugin = {"junit:junitResult.xml", "html:output.html"},
                 features = "src/test/resources/feature/manualRunner")
public class ManualRunnerTest {

}


