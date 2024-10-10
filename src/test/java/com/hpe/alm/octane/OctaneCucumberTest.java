package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.Constants;
import io.cucumber.junit.Cucumber;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.stream.Collectors;

public class OctaneCucumberTest {

  @Test
  public void testRunCucumberFeatureFile() throws IOException, InitializationError {
    test(RunCucumberFeatureFile.class);
  }

  @Test
  public void testRunCucumberFeatureClassPath() throws IOException, InitializationError {
    test(RunCucumberFeatureClassPath.class);
  }

  @Test
  public void testBackgroundStepFails() throws IOException, InitializationError {
    test(BackgroundStepFails.class);
  }

  @Test
  public void testStepNotImplemented() throws IOException, InitializationError {
    test(StepNotImplemented.class);
  }

  @Test
  public void testRunCucumberCustomResultsFolder() throws IOException, InitializationError {
    test(RunCucumberCustomResultsFolder.class, "a/b/");
  }

  @Test
  public void testRunMultipleRuleFeatureTest() throws IOException, InitializationError {
    test(RunMultipleRuleFeatureTest.class);
  }

  private void test(Class classToTest) throws IOException, InitializationError {
    test(classToTest, "");
  }

  private void test(Class classToTest, String subFolder) throws IOException, InitializationError {
    Cucumber runner = new Cucumber(classToTest);
    runner.run(new RunNotifier());
    validate(classToTest, subFolder);
  }


  private void validate(Class classToTest, String subFolder) throws FileNotFoundException {
    String resultFileName = classToTest.getSimpleName() + ".xml";

    URL resource = getClass().getClassLoader().getResource("expectedResults/" + resultFileName);
    String expectedXml = "";
    if (resource != null) {
      FileReader fileReader = new FileReader(resource.getFile());
      BufferedReader expectedResultFileReader = new BufferedReader(fileReader);
      expectedXml = expectedResultFileReader.lines().collect(Collectors.joining());
      //TODO - IS THIS CORRECT?!!
      expectedXml = expectedXml.replace("[ROOT_PATH]","\\C:\\dev\\octane-cucumber-jvm\\");
    }

    BufferedReader actualResultFileReader = new BufferedReader(new FileReader(Constants.RESULTS_FOLDER + "/" + subFolder + resultFileName));
    String actualXml = actualResultFileReader.lines().collect(Collectors.joining());

    validatePath(expectedXml, actualXml);

    expectedXml = expectedXml
        .replaceAll("\\s+", "");

    actualXml = actualXml
        .replaceAll(" duration=\"\\d*\"", "")
        .replaceAll(" started=\"\\d*\"", "")
        .replaceAll("\\s+", "");

    Assert.assertEquals(expectedXml, actualXml);
  }

  private void validatePath(String expected, String actual) {
    int expectedPathStart = expected.indexOf("path=");
    int expectedPathEnd = expected.indexOf("\"", expectedPathStart + 7);
    String expectedPath = expected.substring(expectedPathStart + 6, expectedPathEnd);

    int actualPathStart = actual.indexOf("path=");
    int actualPathEnd = actual.indexOf("\"", actualPathStart + 7);
    String actualPath = actual.substring(actualPathStart, actualPathEnd);
    String actualPathSuffix = actualPath.substring(actualPath.length() - expectedPath.length());

    Assert.assertEquals("Path suffix not equal", expectedPath, actualPathSuffix);
  }
}
