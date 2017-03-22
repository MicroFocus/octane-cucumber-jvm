package com.hpe.alm.octane.infra;

import cucumber.runtime.CucumberException;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class OutputFile {
	private Class testClass;

	public OutputFile(Class testClass) {
		this.testClass = testClass;
	}

	public void write(Document doc) {
		File file = getOutputFile(testClass);
		try (FileOutputStream outputStream = new FileOutputStream(file)) {
			DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
			DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
			LSSerializer serializer = impl.createLSSerializer();
			LSOutput output = impl.createLSOutput();
			output.setByteStream(outputStream);
			serializer.write(doc, output);
		} catch (Exception e) {
			throw new CucumberException(Constants.errorPrefix + "Failed to write document to disc", e);
		}
	}

	public File getOutputFile(Class testClass) {
		if(testClass == null) {
			throw new IllegalArgumentException("testClass cannot be null");
		}

		try {
			File outputFolder = new File(Constants.RESULTS_FOLDER);
			Files.createDirectories(outputFolder.toPath()); // if directory already exists will do nothing

			//testClass.getName() is the unique identifier of the class
			String fileName = String.format("%s_%s", testClass.getName(), Constants.RESULTS_FILE_NAME_POSTFIX);
			File outputFile = new File(outputFolder, fileName);
			outputFile.createNewFile(); // if file already exists will do nothing
			return outputFile;
		} catch (IOException e) {
			throw new CucumberException(Constants.errorPrefix + "Failed to create cucumber results output directory", e);
		}
	}
}
