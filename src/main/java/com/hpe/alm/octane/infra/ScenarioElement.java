package com.hpe.alm.octane.infra;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by intract on 23/06/2016.
 */
public class ScenarioElement implements GherkinSerializer {
    private String _name;
    private Integer index;
    private List<StepElement> _steps;
    private Integer _outlineIndex = 0;
    private String type = "Scenario";

    public ScenarioElement(Integer index, String name, int _outlineIndex) {
        this(index, name);
        this._outlineIndex = _outlineIndex;
    }

    public ScenarioElement(Integer index, String name, String type) {
        this(index, name);
        this.type = type;
    }


    public ScenarioElement(Integer index, String name) {
        _name = name;
        this.index = index;
        _steps = new ArrayList<>();
    }

    public List<StepElement> getSteps() {
        return _steps;
    }

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
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