package com.hpe.alm.octane.infra;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by intract on 23/06/2016.
 */
public class FeatureElement implements GherkinSerializer {
    private String name = "";
    private String tag = "";
    private String path = "";
    private String fileContent = "";
    private Long started;
    private List<ScenarioElement> scenarios;
    private List<StepElement> backgroundSteps;

    public FeatureElement() {
        scenarios = new ArrayList<>();
        backgroundSteps = new ArrayList<>();
    }

    public List<ScenarioElement> getScenarios() {
        return scenarios;
    }

    public List<StepElement> getBackgroundSteps() {
        return backgroundSteps;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFileContent(String _fileContent) {
        this.fileContent = _fileContent;
    }

    public void setStarted(Long started) { this.started = started; }

    public Element toXMLElement(Document doc) {
        Element feature = doc.createElement(GherkinSerializer.FEATURE_TAG_NAME);

        // Adding the feature members
        feature.setAttribute("name", name);
        feature.setAttribute("path", path);
        feature.setAttribute("tag", tag);
        if (started != null) {
            feature.setAttribute("started", started.toString());
        }

        // Adding the file to the feature
        Element fileElement = doc.createElement(GherkinSerializer.FILE_TAG_NAME);
        fileElement.appendChild(doc.createCDATASection(fileContent));
        feature.appendChild(fileElement);

        Element scenariosElement = doc.createElement(GherkinSerializer.SCENARIOS_TAG_NAME);

        // Serializing the background
        if (backgroundSteps != null && backgroundSteps.size()>0) {
            Element backgroundStepsElement = doc.createElement(GherkinSerializer.STEPS_TAG_NAME);
            for (StepElement step : backgroundSteps) {
                backgroundStepsElement.appendChild(step.toXMLElement(doc));
            }

            Element backgroundElement = doc.createElement(GherkinSerializer.BACKGROUND_TAG_NAME);
            backgroundElement.appendChild(backgroundStepsElement);
            scenariosElement.appendChild(backgroundElement);
        }

        // Serializing the scenarios
        for (ScenarioElement scenario : scenarios) {
            scenariosElement.appendChild(scenario.toXMLElement(doc));
        }

        feature.appendChild(scenariosElement);

        return feature;
    }
}

