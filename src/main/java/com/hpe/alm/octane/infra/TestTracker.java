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
        if (isOutlineScenario(scenarioName)) {
            if (isFirstOutlineScenario(currentScenario)) {
                currentScenario.setOutlineIndex(++outlineIndex);
            }
            currentScenario = new ScenarioElement(scenarioName, ++outlineIndex);
        } else {
            outlineIndex = 0;
            currentScenario = new ScenarioElement(scenarioName, outlineIndex);
        }
        currentFeature.addScenario(currentScenario);
    }

    private boolean isOutlineScenario(String scenarioName) {
        return currentScenario != null && currentScenario.getName().equals(scenarioName);
    }

    private boolean isFirstOutlineScenario(ScenarioElement scenario) {
        return scenario.getOutlineIndex() == 0;
    }

    public void setCurrentStep(StepElement step) {
        if (currentScenario == null) {
            ErrorHandler.error("Flow error - Tried to set step when there is no current scenario");
        }
        currentScenario.addStep(step);
    }

    public StepElement getCurrentStep() {
        if (currentScenario == null) {
            ErrorHandler.error("Flow error - Tried to get current step when there is no current scenario");
        }
        if (currentScenario.getSteps().size() < 1) {
            ErrorHandler.error("Flow error - Tried to get current step when there are no steps in the current scenario");
        }
        return currentScenario.getSteps().get(currentScenario.getSteps().size()-1);
    }

    public List<FeatureElement> getFeatures() {
        return Collections.unmodifiableList(features);
    }
}
