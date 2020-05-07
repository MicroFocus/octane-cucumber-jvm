package com.hpe.alm.octane.infra;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

public interface GherkinSerializer {
  static final String FEATURE_TAG_NAME = "feature";
  static final String ROOT_TAG_NAME = "features";
  static final String SCENARIO_TAG_NAME = "scenario";
  static final String SCENARIOS_TAG_NAME = "scenarios";
  static final String FILE_TAG_NAME = "file";
  static final String STEP_TAG_NAME = "step";
  static final String STEPS_TAG_NAME = "steps";
  static final String ERROR_MESSAGE_TAG_NAME = "error_message";

  Element toXMLElement(Document doc);

  default String sanitizePath(String path) {
    String sanitizedPath = null;
    try {
      sanitizedPath = new URI(path)
          .getSchemeSpecificPart()
          .replace('/', File.separatorChar);
    } catch (URISyntaxException e) {
      ErrorHandler.error("Can't sanitize path: " + path, e);
    }
    return sanitizedPath;
  }
}
