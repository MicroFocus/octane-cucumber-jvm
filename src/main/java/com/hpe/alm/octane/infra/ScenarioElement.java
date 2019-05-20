package com.hpe.alm.octane.infra;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by intract on 23/06/2016.
 */
public class ScenarioElement implements GherkinSerializer {
    private String name;
    private List<StepElement> steps = new ArrayList<>();
    private Integer outlineIndex = 0;

    public ScenarioElement(String name, int outlineIndex) {
        this.name = name;
        this.outlineIndex = outlineIndex;
    }

    public void setOutlineIndex(int outlineIndex) {
        this.outlineIndex = outlineIndex;
    }

    public List<StepElement> getSteps() {
        return steps;
    }

    public void addStep(StepElement step) {
        steps.add(step);
    }

    public int getOutlineIndex() {
        return outlineIndex;
    }

    public String getName() {
        return name;
    }

    public Element toXMLElement(Document doc) {
        // Adding the feature members
        Element scenario = doc.createElement(SCENARIO_TAG_NAME);
        scenario.setAttribute("name", name);
        if(outlineIndex > 0){
            scenario.setAttribute("outlineIndex", outlineIndex.toString());
        }

        // Serializing the steps
        Element steps = doc.createElement(STEPS_TAG_NAME);
        for (StepElement step : this.steps) {
            steps.appendChild(step.toXMLElement(doc));
        }

        scenario.appendChild(steps);

        return scenario;
    }
}