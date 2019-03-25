package com.hpe.alm.octane.infra;

import gherkin.ast.TableRow;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by intract on 23/06/2016.
 */
public class ScenarioElement implements GherkinSerializer {
    private String name;
    private Integer index;
    private List<StepElement> steps;
    private Integer outlineIndex = 0;
    private String type;

    public ScenarioElement(Integer index, String name, String type, int outlineIndex) {
        this(index, name, type);
        this.outlineIndex = outlineIndex;
    }

    public ScenarioElement(Integer index, String name, String type) {
        this(index, name);
        this.type = type;
    }

    public ScenarioElement(Integer index, String name) {
        this.index = index;
        this.name = name;
        steps = new ArrayList<>();
    }

    public List<StepElement> getSteps() {
        return steps;
    }

    public Integer getIndex() {
        return index;
    }

    public String getName() {
        return name;
    }

    public boolean isBackgroundScenario(){
        return type.equals("Background");
    }

    public boolean isScenarioOutline(){
        return type.equals("Scenario Outline");
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