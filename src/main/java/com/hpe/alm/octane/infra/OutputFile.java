package com.hpe.alm.octane.infra;

import org.w3c.dom.Document;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import java.io.OutputStream;
import java.net.URL;

public class OutputFile {
  URL url;

  public OutputFile(URL url) {
    this.url = url;
  }

  public void write(Document doc) {
    try (OutputStream outputStream = new ResultFileOutputStream(url)) {
      DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
      DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
      LSSerializer serializer = impl.createLSSerializer();
      serializer.getDomConfig().setParameter("format-pretty-print", Boolean.TRUE);
      LSOutput output = impl.createLSOutput();
      output.setByteStream(outputStream);
      serializer.write(doc, output);
    } catch (Exception e) {
      ErrorHandler.error("Failed to write the result XML to the file system.", e);
    }
  }

}
