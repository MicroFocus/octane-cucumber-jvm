# octane-cucumber-jvm
ALM Octane cucumber-jvm formatter enable uploading cucumber-jvm tests back into ALM Octane.

## How does it work
1. You use this plugin in your JUnit cucumber tests (See instructions below)
2. When running the tests, the plugin outputs _OctaneGherkinResults.xml files with the results
3. Octane plugin for Jenkins reads these files and upload the results back to Octane (read how to configure Octane Jenkins plugin from Octane online help)
4. You can see the results in your Gherkin test in Octane

## Prerequisites:
* You are using Java language and the cucumber-jvm library to develop cucumber tests
* You are using Junit Runner to run the cucumber-jvm library like explained in the [following link](https://cucumber.io/docs/reference/jvm#junit-runner)

The JUnit runner uses the JUnit framework to run Cucumber.
The default configuration require a single empty class with an annotation that looks like this:
```java
/**********************************
  before the plugin configuration
***********************************/
package mypackage;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
public class RunCukesTest {

}
```

# How to configure octane-cucumber-jvm in your project:
1. Add a dependency in your pom file:
```xml
<dependencies>
    <dependency>
        <groupId>com.hpe.alm.octane</groupId>
        <artifactId>octane-cucumber-jvm</artifactId>
        <version>12.55.7</version>
    </dependency>
</dependencies>
```

2. Import the formatter into the Junit Runner class (see example below)
```java
import com.hpe.alm.octane.OctaneCucumber;
```

3. Change the cucumber.class to OctaneCucumber.class. See a full example:
```java
package feature.manualRunner;

import com.hpe.alm.octane.OctaneCucumber;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(plugin={"junit:junitResult.xml"},
    features="src/test/resources/feature/manualRunner")
public class ManualRunnerTest{

}
```

