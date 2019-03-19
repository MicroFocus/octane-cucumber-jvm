package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.*;
import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.*;
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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//todo - need to check it with Gherkin 2.0

public class OctaneGherkinFormatter implements EventListener {
    private Document _doc;
    private Element _rootElement;
    private OutputFile outputFile;
    private ScenarioElement _currentScenario = null;
    private StepElement _currentStep = null;
    private Map<String, FeatureElement> _features = new LinkedHashMap<>();
    private List<StepElement> _backgroundSteps = null;
    private Integer _scenarioOutlineIndex = null;
    private Map<String, GherkinDocument> cucumberFeatures = new HashMap<>();

    private EventHandler<TestSourceRead> testSourceReadHandler = this::handleSourceRead;
    private EventHandler<TestCaseStarted> testCaseStartedHandler = this::handleTestCaseStarted;
    private EventHandler<TestCaseFinished> testCaseFinishedHandler = this::handleTestCaseFinished;
    private EventHandler<TestStepStarted> stepStartedHandler = this::handleTestStepStarted;
    private EventHandler<TestStepFinished> stepFinishedHandler = this::handleTestStepFinished;
    private EventHandler<TestRunFinished> runFinishedHandler = event -> finishReport();

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

    private void initFeatures(List<CucumberFeature> features){
        features.forEach(cucumberFeature -> cucumberFeatures.put(cucumberFeature.getUri(), cucumberFeature.getGherkinFeature()));
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, testSourceReadHandler);
        publisher.registerHandlerFor(TestCaseStarted.class, testCaseStartedHandler);
        publisher.registerHandlerFor(TestCaseFinished.class, testCaseFinishedHandler);
        publisher.registerHandlerFor(TestStepStarted.class, stepStartedHandler);
        publisher.registerHandlerFor(TestStepFinished.class, stepFinishedHandler);
        publisher.registerHandlerFor(TestRunFinished.class, runFinishedHandler);
    }

    /**************** Source Read *****************/
    private void handleSourceRead(TestSourceRead event) {
        Feature feature = cucumberFeatures.get(event.uri).getFeature();
        List<ScenarioDefinition> scenarioDefinitions = feature.getChildren();

        FeatureElement featureElement = new FeatureElement();
        scenarioDefinitions.forEach(scenarioDefinition -> {
            ScenarioElement scenarioElement = new ScenarioElement(scenarioDefinition.getName(), scenarioDefinition.getKeyword());
            featureElement.getScenarios().add(scenarioElement);
        });

        featureElement.setName(feature.getName());
        featureElement.setPath(event.uri);
        featureElement.setFileContent(event.source);
        featureElement.setStarted(event.getTimeStamp());
        for(Tag tag : feature.getTags()) {
            if(tag.getName().startsWith("@TID")) {
                featureElement.setTag(tag.getName());
                break;
            }
        }
        _features.put(event.uri, featureElement);
    }

    /**************** Test Case ******************/
    private void handleTestCaseStarted(TestCaseStarted event) {
        TestCase scenario = event.getTestCase();
        FeatureElement featureElement = _features.get(scenario.getUri());
        if(isScenarioOutline(scenario)){
            _currentScenario = new ScenarioElement(scenario.getName(),_scenarioOutlineIndex++);
        } else {
            //this is a simple scenario
            _currentScenario = new ScenarioElement(scenario.getName());
            _scenarioOutlineIndex = null;
        }
    }

    private boolean isScenarioOutline(TestCase scenario){
        return _scenarioOutlineIndex != null && scenario.getScenarioDesignation().compareTo("Scenario Outline") == 0;
    }

    private void handleTestCaseFinished(TestCaseFinished event) {
        TestCase testCase = event.getTestCase();
        FeatureElement featureElement = _features.get(testCase.getUri());
        if(_currentScenario != null){
            featureElement.getScenarios().add(_currentScenario);
        }

        if (_backgroundSteps != null && featureElement.getBackgroundSteps().size() == 0) {
            featureElement.getBackgroundSteps().addAll(_backgroundSteps);
        }
        _currentScenario = null;
    }

    /**************** Test Step ******************/
    private void handleTestStepStarted(TestStepStarted event){
        PickleStepTestStep testStep = (PickleStepTestStep)event.testStep;
        if(isScenarioOutlineStep()){
            // no need to keep generic steps - skip them
            return;
        }

        _currentStep = new StepElement(testStep.getStepText(), testStep.getPickleStep().getLocations().get(0).getLine());

        if (_currentScenario != null) {
            _currentScenario.getSteps().add(_currentStep);
        }

        if (_backgroundSteps != null) {
            _currentStep.setBackgroundStep();
            _backgroundSteps.add(_currentStep);
        }
    }

    private void handleTestStepFinished(TestStepFinished event) {
        Result result = event.result;
        if (_currentStep != null) {
            _currentStep.setStatus(result.getStatus().name());
            _currentStep.setDuration(result.getDuration());
            if(result.getErrorMessage() != null){
                _currentStep.setErrorMessage(result.getErrorMessage());
            }
            _currentStep = null;
        }

    }

    private boolean isScenarioOutlineStep(){
        return _scenarioOutlineIndex != null && _scenarioOutlineIndex == 1;
    }
//
//    public void match(Match match) {
//        if(match == Match.UNDEFINED){
//            _currentStep = null;
//            return;
//        }
//
//        if (_currentScenario != null) {
//            setCurrentStep(_currentScenario.getSteps(), (StepDefinitionMatch)match);
//        } else if (_backgroundSteps != null) {
//            setCurrentStep(_backgroundSteps, (StepDefinitionMatch)match);
//        }
//    }
//
//    private void setCurrentStep(List<StepElement> steps, StepDefinitionMatch match) {
//        for (StepElement step : steps) {
//            // Checking if it's the same step
//            if (step.getLine() == match.getStepLocation().getLineNumber()) {
//                _currentStep = step;
//            }
//        }
//    }
    /**************** Test Step ******************/

    private void finishReport(){
        _features.values().forEach(featureElement -> _rootElement.appendChild(featureElement.toXMLElement(_doc)));
        outputFile.write(_doc);
    }
}

//    public void endOfScenarioLifeCycle(Scenario scenario) {
//        if(_currentScenario != null){
//            _currentFeature.getScenarios().add(_currentScenario);
//        }
//
//        if (_backgroundSteps != null && _currentFeature.getBackgroundSteps().size()==0) {
//            _currentFeature.getBackgroundSteps().addAll(_backgroundSteps);
//        }
//        _currentScenario = null;
//    }