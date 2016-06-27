package infra;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by intract on 23/06/2016.
 */
public class FeatureElement implements GherkinSerializer {
    private String _name = "";
    private String _tag = "";
    private String _path = "";
    private String _file = "";
    private Long _started;
    private List<ScenarioElement> _scenarios;
    private List<StepElement> _backgroundSteps;

    public FeatureElement() {
        _scenarios = new ArrayList<>();
        _backgroundSteps = new ArrayList<>();
    }

    public List<ScenarioElement> getScenarios() {
        return _scenarios;
    }

    public List<StepElement> getBackgroundSteps() {
        return _backgroundSteps;
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setTag(String tag) {
        this._tag = tag;
    }

    public void setPath(String path) {
        this._path = path;
    }

    public void setFile(String file) {
        this._file = file;
    }

    public void setStarted(Long started) { this._started = started; }

    public Element toXMLElement(Document doc) {
        Element feature = doc.createElement(GherkinSerializer.FEATURE_TAG_NAME);

        // Adding the feature members
        feature.setAttribute("name", _name);
        feature.setAttribute("path", _path);
        feature.setAttribute("tag", _tag);
        if (_started != null) {
            feature.setAttribute("started", _started.toString());
        }

        // Adding the file to the feature
        Element fileElement = doc.createElement(GherkinSerializer.FILE_TAG_NAME);
        fileElement.appendChild(doc.createCDATASection(_file));
        feature.appendChild(fileElement);

        Element scenariosElement = doc.createElement(GherkinSerializer.SCENARIOS_TAG_NAME);

        // Serializing the background
        if (_backgroundSteps != null && _backgroundSteps.size()>0) {
            Element backgroundStepsElement = doc.createElement(GherkinSerializer.STEPS_TAG_NAME);
            for (StepElement step : _backgroundSteps) {
                backgroundStepsElement.appendChild(step.toXMLElement(doc));
            }

            Element backgroundElement = doc.createElement(GherkinSerializer.BACKGROUND_TAG_NAME);
            backgroundElement.appendChild(backgroundStepsElement);
            scenariosElement.appendChild(backgroundElement);
        }

        // Serializing the scenarios
        for (ScenarioElement scenario : _scenarios) {
            scenariosElement.appendChild(scenario.toXMLElement(doc));
        }

        feature.appendChild(scenariosElement);

        return feature;
    }
}

