package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.Constants;
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

	private void test(Class classToTest) throws IOException, InitializationError {
		OctaneCucumber runner = new OctaneCucumber(classToTest);
		runner.run(new RunNotifier());
		validate(classToTest);
	}

	private void validate(Class classToTest) throws FileNotFoundException {
		String resultFileName = String.format("%s_%s", classToTest.getName(), Constants.RESULTS_FILE_NAME_POSTFIX);

		URL resource = getClass().getClassLoader().getResource("expectedResults/" + resultFileName);
		FileReader fileReader = new FileReader(resource.getFile());
		BufferedReader expectedResultFileReader = new BufferedReader(fileReader);
		String expectedXml = expectedResultFileReader.lines().collect(Collectors.joining());

		BufferedReader actualResultFileReader = new BufferedReader(new FileReader(Constants.RESULTS_FOLDER + "/" + resultFileName));
		String actualResultFileContent = actualResultFileReader.lines().collect(Collectors.joining());
		String actualXml = actualResultFileContent.replaceAll(" duration=\"\\d*\"", "").replaceAll(" started=\"\\d*\"", "");

		Assert.assertEquals(expectedXml, actualXml);
	}
}
