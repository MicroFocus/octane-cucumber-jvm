package com.hpe.alm.octane.infra;

import cucumber.runtime.CucumberException;
import cucumber.runtime.io.URLOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.*;
import java.net.URL;

public class OutputFile {
    URL url;

    public OutputFile(URL url) {
        this.url = url;
    }

	public void write(Document doc) {
		try (OutputStream outputStream = new URLOutputStream(url)) {
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
}
