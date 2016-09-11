# octane-cucumber-jvm
ALM Octane cucumber-jvm formatter 

This plugin enable uploading cucumber-jvm tests back into ALM Octane.

## Prerequisites:
* You are using Java language and the cucumber-jvm library to develop cucumber tests
* You are using Junit Runner to run the cucumber-jvm library like explained in the [following link](https://cucumber.io/docs/reference/jvm#junit-runner)

The JUnit runner uses the JUnit framework to run Cucumber. 
The default configuration require a single empty class with an annotation that looks like this:
```
package mypackage;
import cucumber.api.junit.Cucumber;
import org.junit.runner.RunWith;

@RunWith(Cucumber.class)
public class RunCukesTest {

}
```

## How to configure in your project:
1. Add a dependency in your pom file:
```
<dependencies>
    <dependency>
        <groupId>com.hpe.alm.octane</groupId>
        <artifactId>octane-cucumber-jvm</artifactId>
        <version>12.53.8</version>
    </dependency>
</dependencies>
```

2. Import the formatter into the Junit Runner class (see example below)
```
import com.hpe.alm.octane.OctaneCucumber;
```

3. Change the cucumber.class to OctaneCucumber.class. See a full example:
```
Package feature.manualRunner;

Import com.hpe.alm.octane.OctaneCucumber;
Import cucumber.api.CucumberOptions;
Import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(plugin={"junit:junitResult.xml"},
    features="src/test/resources/feature/manualRunner")
Public class ManualRunnerTest{

}
```

