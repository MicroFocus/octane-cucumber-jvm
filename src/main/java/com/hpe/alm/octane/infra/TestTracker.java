package com.hpe.alm.octane.infra;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TestTracker {
    private List<FeatureElement> features = new ArrayList<>();
    private FeatureElement currentFeature;
    private ScenarioElement currentScenario;
    private int outlineIndex = 0;

    public void setCurrentFeature(FeatureElement feature) {
        features.add(feature);
        currentFeature = feature;
    }

    public FeatureElement getCurrentFeature() {
        return currentFeature;
    }

    public void setCurrentScenario(String scenarioName) {
        if (currentScenario != null && currentScenario.getName().equals(scenarioName)) {
            if (currentScenario.getOutlineIndex() == 0) {
               currentScenario.setOutlineIndex(1);
               outlineIndex = 1;
            }
            currentScenario = new ScenarioElement(scenarioName, ++outlineIndex);
        } else {
            currentScenario = new ScenarioElement(scenarioName, 0);
            outlineIndex = 0;
        }
        currentFeature.addScenario(currentScenario);
    }

    public void setCurrentStep(StepElement step) {
        currentScenario.addStep(step);
    }

    public StepElement getCurrentStep() {
        return currentScenario.getSteps().get(currentScenario.getSteps().size()-1);
    }

    public List<FeatureElement> getFeatures() {
        return Collections.unmodifiableList(features);
    }
}
