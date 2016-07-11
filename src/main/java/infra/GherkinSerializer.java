package infra;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface GherkinSerializer {
    static final String FEATURE_TAG_NAME = "feature";
    static final String RESULTS_FILE_NAME = "gherkinNGAResults.xml_";
    static final String ROOT_TAG_NAME = "features";
    static final String SCENARIO_TAG_NAME = "scenario";
    static final String SCENARIOS_TAG_NAME = "scenarios";
    static final String FILE_TAG_NAME = "file";
    static final String STEP_TAG_NAME = "step";
    static final String STEPS_TAG_NAME = "steps";
    static final String BACKGROUND_TAG_NAME = "background";
    static final String ERROR_MESSAGE_TAG_NAME = "error_message";

    Element toXMLElement(Document doc);
}
