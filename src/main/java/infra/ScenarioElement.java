package infra;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by intract on 23/06/2016.
 */
public class ScenarioElement implements GherkinSerializer {
    private String _name = "";
    private List<StepElement> _steps;
    private Integer _outlineIndex = 0;

    public ScenarioElement(String name, int outlineIndex) {
        this(name);
        _outlineIndex = outlineIndex;
    }

    public ScenarioElement(String name) {
        _name = name;
        _steps = new ArrayList<>();
    }

    public List<StepElement> getSteps() {
        return _steps;
    }

    public Element toXMLElement(Document doc) {
        // Adding the feature members
        Element scenario = doc.createElement(SCENARIO_TAG_NAME);
        scenario.setAttribute("name", _name);
        if(_outlineIndex>0){
            scenario.setAttribute("outlineIndex", _outlineIndex.toString());
        }

        // Serializing the steps
        Element steps = doc.createElement(STEPS_TAG_NAME);
        for (StepElement step : _steps) {
            steps.appendChild(step.toXMLElement(doc));
        }

        scenario.appendChild(steps);

        return scenario;
    }
}