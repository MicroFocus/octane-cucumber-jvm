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
    private StepElement _currentStep = null;
    private Map<String, FeatureElement> _features = new LinkedHashMap<>();
    private List<StepElement> _backgroundSteps = new ArrayList<>();
    private Integer _scenarioOutlineIndex = null;
    private Integer _scenarioIndex = null;
    private Integer _stepIndex = null;
    private Map<String, GherkinDocument> cucumberFeatures = new HashMap<>();

    private EventHandler<TestSourceRead> testSourceReadHandler = this::handleSourceRead;
    private EventHandler<TestCaseStarted> testCaseStartedHandler =  event -> handleTestCaseStarted();
    private EventHandler<TestCaseFinished> testCaseFinishedHandler = this::handleTestCaseFinished;
    private EventHandler<TestStepStarted> stepStartedHandler = event -> handleTestStepStarted();
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
        FeatureElement featureElement = new FeatureElement();
        List<ScenarioDefinition> scenarioDefinitions = feature.getChildren();
        int scenarioIndex = 0;
        for(ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
            ScenarioElement scenarioElement = new ScenarioElement(scenarioIndex, scenarioDefinition.getName(), scenarioDefinition.getKeyword());
            //todo - if scenario is outline, update scenario index
            scenarioDefinition.getSteps().forEach(step -> {
                StepElement stepElement = new StepElement(step.getKeyword(), step.getText(), step.getLocation().getLine());
                if(scenarioDefinition.getKeyword().equals("Background")) {
                    stepElement.setBackgroundStep();
                    _backgroundSteps.add(stepElement);
                }
                scenarioElement.getSteps().add(stepElement);
                //todo - ignore outline generic step
            });
            featureElement.getScenarios().add(scenarioElement);
            if (_backgroundSteps != null && featureElement.getBackgroundSteps().size() == 0) {
                featureElement.getBackgroundSteps().addAll(_backgroundSteps);
            }
            scenarioIndex++;
        }

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
        _features.put(event.uri, featureElement);
        _scenarioIndex = -1;
    }

    private String findAbsolutePath(String uri) {
        URL res = getClass().getClassLoader().getResource(uri);
        File file = null;
        try {
            file = Paths.get(res.toURI()).toFile();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String absolutePath = file.getAbsolutePath();
        if (absolutePath.contains(uri.replace('/', File.separatorChar))) {
            return absolutePath;
        }
        return null;
    }

        /**************** Test Case ******************/
        private void handleTestCaseStarted() {
            _scenarioIndex++;
            _stepIndex = -1;
//        TestCase scenario = event.getTestCase();
//        if(isScenarioOutline(scenario)){
//            _currentScenario = new ScenarioElement(_scenarioIndex, scenario.getName(),_scenarioOutlineIndex++);
//        } else {
//            //this is a simple scenario
//            _currentScenario = new ScenarioElement(_scenarioIndex, scenario.getName());
//            _scenarioOutlineIndex = null;
//        }
        }

        private boolean isScenarioOutline(TestCase scenario){
            return _scenarioOutlineIndex != null && scenario.getScenarioDesignation().compareTo("Scenario Outline") == 0;
        }

        private void handleTestCaseFinished(TestCaseFinished event) {
            TestCase testCase = event.getTestCase();
            FeatureElement featureElement = _features.get(testCase.getUri());
            if (_backgroundSteps != null && featureElement.getBackgroundSteps().size() == 0) {
                featureElement.getBackgroundSteps().addAll(_backgroundSteps);
            }
        }

        /**************** Test Step ******************/
        private void handleTestStepStarted(){
            _stepIndex++;
        }

        private void handleTestStepFinished(TestStepFinished event) {
            ScenarioElement scenarioElement = getCurrentScenario(event.getTestCase());
            if(scenarioElement != null) {
                StepElement stepElement;
                if (scenarioElement.getIndex() != 0) {
                    stepElement = getCurrentStep(scenarioElement);
                } else {
                    FeatureElement featureElement = getCurrentFeature(event.getTestCase());
                    stepElement = featureElement.getBackgroundSteps().get(0);
                }
                Result result = event.result;
                //todo - ignore outline generic step
                if (stepElement != null) {
                    stepElement.setStatus(result.getStatus().name());
                    stepElement.setDuration(result.getDuration());
                    if (result.getErrorMessage() != null) {
                        stepElement.setErrorMessage(result.getErrorMessage());
                    }
                }
            }
        }

        private StepElement getCurrentStep(ScenarioElement scenarioElement){
            return scenarioElement.getSteps().get(_stepIndex);
        }

        private ScenarioElement getCurrentScenario(TestCase testCase){
            FeatureElement featureElement = getCurrentFeature(testCase);
            Optional<ScenarioElement> optionalScenario = featureElement.getScenarios().stream()
                    .filter(scenarioElement1 -> scenarioElement1.getIndex().equals(_scenarioIndex)).findFirst();

            return optionalScenario.orElse(null);
        }

        private FeatureElement getCurrentFeature(TestCase testCase){
            return _features.get(testCase.getUri());
        }

        private boolean isScenarioOutlineStep(){
            return _scenarioOutlineIndex != null && _scenarioOutlineIndex == 1;
        }

        /**************** Test Step ******************/

        private void finishReport(){
            _features.values().forEach(featureElement -> _rootElement.appendChild(featureElement.toXMLElement(_doc)));
            outputFile.write(_doc);
        }
    }