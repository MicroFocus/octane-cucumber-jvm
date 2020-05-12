# octane-cucumber-jvm
ALM Octane cucumber-jvm formatter enables uploading cucumber-jvm tests back into ALM Octane.

## How does it work
1.	You use this plugin in your JUnit Cucumber tests (see instructions below).
2.	When running the tests, the plugin outputs files with the results.
3.	ALM Octane plugin for Jenkins reads these files and uploads the results back to ALM Octane (see how to configure ALM Octane Jenkins plugin in the ALM Octane online help).
4.	You can see the results in your Gherkin test in ALM Octane.

## Prerequisites:
* You are using Java language and the cucumber-jvm library to develop Cucumber tests.
* You are using Junit Runner to run the cucumber-jvm library as explained [here](https://cucumber.io/docs/cucumber/api/#running-cucumber)

The JUnit runner uses the JUnit framework to run Cucumber. The default configuration require a single empty class with an annotation:
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

## For Cucumber 4.3
1. Add a dependency in your pom file:
```xml
<dependencies>
    <dependency>
        <groupId>com.hpe.alm.octane</groupId>
        <artifactId>octane-cucumber-jvm</artifactId>
        <version>15.1.0</version>
    </dependency>
</dependencies>
```

2. Add the OctaneGherkinFormatter plugin to the CucumberOptions of each test class, and provide a unique result file path:
```java
package feature.manualRunner;

import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
@CucumberOptions(plugin="com.hpe.alm.octane.OctaneGherkinFormatter:gherkin-results/ManualRunnerTest_OctaneGherkinResults.xml",
    features="src/test/resources/feature/manualRunner")
public class ManualRunnerTest{

}
```

## For cucumber 1.2
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

2. Import the formatter into the Junit Runner class:
```java
import com.hpe.alm.octane.OctaneCucumber;
```

3. Change the cucumber.class to OctaneCucumber.class:
```java
package feature.manualRunner;

import com.hpe.alm.octane.OctaneCucumber;
import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(features="src/test/resources/feature/manualRunner")
public class ManualRunnerTest{

}
```
