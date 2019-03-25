package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.*;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.*;
import cucumber.api.event.EventListener;
import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Feature;
import gherkin.ast.GherkinDocument;
import gherkin.ast.ScenarioDefinition;
import gherkin.ast.Tag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;

//todo - need to check it with Gherkin 2.0

public class OctaneGherkinFormatter implements EventListener {
    private Document _doc;
    private Element _rootElement;
    private OutputFile outputFile;
    private Map<String, FeatureElement> _features = new LinkedHashMap<>();
    private List<StepElement> backgroundSteps = new ArrayList<>();
    private Integer _scenarioIndex = -1;
    private Integer _scenarioOutlineIndex = 0;
    private Integer _stepIndex = 0;
    private Map<String, GherkinDocument> cucumberFeatures = new HashMap<>();

    private EventHandler<TestSourceRead> testSourceReadHandler = this::handleSourceRead;
    private EventHandler<TestCaseStarted> testCaseStartedHandler =  event -> scenarioStarted();
    private EventHandler<TestCaseFinished> testCaseFinishedHandler = this::scenarioFinished;
   // private EventHandler<TestStepStarted> stepStartedHandler = event -> testStepStarted();
    private EventHandler<TestStepFinished> stepFinishedHandler = this::testStepFinished;
    private EventHandler<TestRunFinished> runFinishedHandler = event -> finishTestReport();

    OctaneGherkinFormatter(List<CucumberFeature> cucumberFeatures, OutputFile outputFile){
        initFeatures(cucumberFeatures);
        this.outputFile = outputFile;
        try {
            _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            _rootElement = _doc.createElement(GherkinSerializer.ROOT_TAG_NAME);
            _rootElement.setAttribute("version",Constants.XML_VERSION);
            _doc.appendChild(_rootElement);
        } catch (ParserConfigurationException e) {
            throw new CucumberException(Constants.errorPrefix + "Failed to create xml document",e);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    private void initFeatures(List<CucumberFeature> features){
        features.forEach(cucumberFeature -> cucumberFeatures.put(cucumberFeature.getUri(), cucumberFeature.getGherkinFeature()));
    }

    private void handleSourceRead(TestSourceRead event) {
        Feature feature = getCurrentFeature(event);
        List<ScenarioDefinition> scenarioDefinitions = feature.getChildren();
        FeatureElement featureElement = new FeatureElement();
        setScenariosInfo(scenarioDefinitions, featureElement);
        setFeatureInfo(event, feature, featureElement);
        _features.put(event.uri, featureElement);
    }

    private Feature getCurrentFeature(TestSourceRead event) {
        return cucumberFeatures.get(event.uri).getFeature();
    }

    private void setScenariosInfo(List<ScenarioDefinition> scenarioDefinitions, FeatureElement featureElement){
        int scenarioIndex = 0;
        for(ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
            ScenarioElement scenarioElement;
            if(isScenarioOutline(scenarioDefinition)) {
                scenarioElement = new ScenarioElement(scenarioIndex, scenarioDefinition.getName(), scenarioDefinition.getKeyword(), _scenarioOutlineIndex++);
                featureElement.getScenarios().add(scenarioElement);
            } else {
                scenarioElement = new ScenarioElement(scenarioIndex, scenarioDefinition.getName(), scenarioDefinition.getKeyword());
                if(isScenarioBackground(scenarioDefinition)) {
                    scenarioDefinition.getSteps().forEach(step -> {
                        StepElement stepElement = new StepElement(step.getKeyword(), step.getText(), step.getLocation().getLine());
                        stepElement.setBackgroundStep();
                        backgroundSteps.add(stepElement);
                    });
                    featureElement.getBackgroundSteps().addAll(backgroundSteps);
                }
                else {
                    scenarioDefinition.getSteps().forEach(step -> {
                        StepElement stepElement = new StepElement(step.getKeyword(), step.getText(), step.getLocation().getLine());
                        scenarioElement.getSteps().add(stepElement);
                    });
                    featureElement.getScenarios().add(scenarioElement);
                }
            }
            scenarioIndex++;
        }
        addBackgroundStepsToScenarios(featureElement);
    }

    private void addBackgroundStepsToScenarios(FeatureElement featureElement) {
        if(backgroundSteps.size() > 0) {
            featureElement.getScenarios().forEach(scenarioElement1 -> {
                scenarioElement1.getSteps().addAll(0, backgroundSteps);
            });
        }
    }

    private boolean isScenarioBackground(ScenarioDefinition scenarioDefinition){
        return scenarioDefinition.getKeyword().equals("Background");
    }

    private boolean isScenarioOutline(ScenarioDefinition scenarioDefinition){
        return scenarioDefinition.getKeyword().equals("Scenario Outline");
    }

    private void setFeatureInfo(TestSourceRead event, Feature feature, FeatureElement featureElement) {
        featureElement.setName(feature.getName());
        featureElement.setPath(findAbsolutePath(event.uri));
        featureElement.setFileContent(event.source);
        featureElement.setStarted(event.getTimeStamp());

        for(Tag tag : feature.getTags()) {
            if(tag.getName().startsWith("@TID")) {
                featureElement.setTag(tag.getName());
                break;
            }
        }
    }

    private String findAbsolutePath(String uri) {
        URL res = getClass().getClassLoader().getResource(uri);
        if(res != null) {
            File file = null;
            try {
                file = Paths.get(res.toURI()).toFile();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            String absolutePath = file != null ? file.getAbsolutePath() : "";
            if (absolutePath.contains(uri.replace('/', File.separatorChar))) {
                return absolutePath;
            }
        }
        return null;
    }

    /**************** Test Case ******************/
    private void scenarioStarted() {
        _scenarioIndex++;
    }

    private void scenarioFinished(TestCaseFinished event) {
        FeatureElement featureElement = getCurrentFeatureElement(event.getTestCase());
        if (backgroundSteps != null && featureElement.getBackgroundSteps().size() == 0) {
            featureElement.getBackgroundSteps().addAll(backgroundSteps);
        }
        _stepIndex = 0;
        _scenarioIndex = -1;
    }

    /**************** Test Step ******************/
    private void testStepFinished(TestStepFinished event) {
        StepElement stepElement;
        ScenarioElement scenarioElement = getCurrentScenarioElement(event.getTestCase());
        if(scenarioElement.isScenarioOutline()){
            return;
        }
        if (!scenarioElement.isBackgroundScenario()) {
            stepElement = getCurrentStepElement(scenarioElement);
        } else {
            FeatureElement featureElement = getCurrentFeatureElement(event.getTestCase());
            stepElement = featureElement.getBackgroundSteps().get(_stepIndex);
        }
        Result result = event.result;
        if (stepElement != null) {
            stepElement.setStatus(result.getStatus().name());
            stepElement.setDuration(result.getDuration());
            if (result.getErrorMessage() != null) {
                stepElement.setErrorMessage(result.getErrorMessage());
            }
        }
        _stepIndex++;
    }

    private StepElement getCurrentStepElement(ScenarioElement scenarioElement){
        return scenarioElement.getSteps().get(_stepIndex);
    }

    private ScenarioElement getCurrentScenarioElement(TestCase testCase){
        FeatureElement featureElement = getCurrentFeatureElement(testCase);
        return featureElement.getScenarios().get(_scenarioIndex);
    }

    private FeatureElement getCurrentFeatureElement(TestCase testCase){
        return _features.get(testCase.getUri());
    }

    private void finishTestReport(){
        _features.values().forEach(featureElement -> _rootElement.appendChild(featureElement.toXMLElement(_doc)));
        outputFile.write(_doc);
    }
}