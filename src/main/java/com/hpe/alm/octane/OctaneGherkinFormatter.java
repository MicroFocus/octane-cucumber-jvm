package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.*;
import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.*;
import cucumber.api.event.EventListener;
import cucumber.runtime.CucumberException;
import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.*;
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
    private Document doc;
    private Element rootElement;
    private OutputFile outputFile;
    private Map<String, FeatureElement> _features = new LinkedHashMap<>();
    private List<StepElement> backgroundSteps = new ArrayList<>();
    private Integer scenarioIndexRead = -1;
    private Integer scenarioIndexWrite = 0;
    private Integer scenarioOutlineIndex = 0;
    private Integer stepIndex = 0;
    private Map<String, GherkinDocument> cucumberFeatures = new HashMap<>();

    private EventHandler<TestSourceRead> testSourceReadHandler = this::handleSourceRead;
    private EventHandler<TestCaseStarted> testCaseStartedHandler =  event -> scenarioStarted();
    private EventHandler<TestCaseFinished> testCaseFinishedHandler = this::scenarioFinished;
    private EventHandler<TestStepFinished> stepFinishedHandler = this::testStepFinished;
    private EventHandler<TestRunFinished> runFinishedHandler = event -> finishTestReport();

    OctaneGherkinFormatter(List<CucumberFeature> cucumberFeatures, OutputFile outputFile){
        initFeatures(cucumberFeatures);
        this.outputFile = outputFile;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            rootElement = doc.createElement(GherkinSerializer.ROOT_TAG_NAME);
            rootElement.setAttribute("version",Constants.XML_VERSION);
            doc.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
            throw new CucumberException(Constants.errorPrefix + "Failed to create xml document", e);
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
        backgroundSteps.clear(); //reset the background steps for the next feature
    }

    private Feature getCurrentFeature(TestSourceRead event) {
        return cucumberFeatures.get(event.uri).getFeature();
    }

    private void setScenariosInfo(List<ScenarioDefinition> scenarioDefinitions, FeatureElement featureElement){
        for(ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
            if(isScenarioBackground(scenarioDefinition)) {
                setBackgroundScenarioInfo(featureElement, scenarioDefinition);
            } else if(isScenarioOutline(scenarioDefinition)) {
                setScenarioOutlineInfo(featureElement, scenarioDefinition);
            } else {
                setRegularScenarioInfo(featureElement, scenarioDefinition);
            }
        }
        addBackgroundStepsToScenarios(featureElement);
    }

    private void setScenarioOutlineInfo(FeatureElement featureElement, ScenarioDefinition scenarioDefinition) {
        ScenarioElement scenarioElement;
        ScenarioOutline scenarioOutline = (ScenarioOutline) scenarioDefinition;
        for (Examples examples : scenarioOutline.getExamples())
        {
            for (TableRow ignored : examples.getTableBody())
            {
                scenarioElement = new ScenarioElement(scenarioIndexWrite++, scenarioDefinition.getName(), scenarioDefinition.getKeyword(), ++scenarioOutlineIndex);
                List<StepElement> steps = new ArrayList<>();
                scenarioDefinition.getSteps().forEach(step -> {
                    StepElement stepElement = new StepElement(step.getText(), step.getKeyword(), step.getLocation().getLine());
                    steps.add(stepElement);
                });
                scenarioElement.getSteps().addAll(steps);
                featureElement.getScenarios().add(scenarioElement);
            }
        }
        scenarioOutlineIndex = 0;
    }

    private void setRegularScenarioInfo(FeatureElement featureElement, ScenarioDefinition scenarioDefinition) {
        ScenarioElement scenarioElement;
        scenarioElement = new ScenarioElement(scenarioIndexWrite++, scenarioDefinition.getName(), scenarioDefinition.getKeyword());
        scenarioDefinition.getSteps().forEach(step -> {
            StepElement stepElement = new StepElement(step.getText(), step.getKeyword(), step.getLocation().getLine());
            scenarioElement.getSteps().add(stepElement);
        });
        featureElement.getScenarios().add(scenarioElement);
    }

    private void setBackgroundScenarioInfo(FeatureElement featureElement, ScenarioDefinition scenarioDefinition) {
        scenarioDefinition.getSteps().forEach(step -> {
            StepElement stepElement = new StepElement(step.getText(), step.getKeyword(), step.getLocation().getLine());
            stepElement.setBackgroundStep();
            backgroundSteps.add(stepElement);
        });
        featureElement.getBackgroundSteps().addAll(backgroundSteps);
    }

    private void addBackgroundStepsToScenarios(FeatureElement featureElement) {
        if(backgroundSteps.size() > 0) {
            featureElement.getScenarios().forEach(scenarioElement ->
                    scenarioElement.getSteps().addAll(0, backgroundSteps));
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
        scenarioIndexRead++;
    }

    private void scenarioFinished(TestCaseFinished event) {
        ScenarioElement scenarioElement = getCurrentScenarioElement(event.getTestCase());
        scenarioElement.getSteps().removeAll(getCurrentFeatureElement(event.getTestCase()).getBackgroundSteps());
        stepIndex = 0;
    }

    /**************** Test Step ******************/
    private void testStepFinished(TestStepFinished event) {
        ScenarioElement scenarioElement = getCurrentScenarioElement(event.getTestCase());
        StepElement stepElement = getCurrentStepElement(scenarioElement);
        setStepInfo(event, stepElement);
        if(scenarioElement.isScenarioOutline()){
            PickleStepTestStep testStep = (PickleStepTestStep)(event.testStep);
            stepElement.setName(testStep.getStepText());
        }
        stepIndex++;
    }

    private void setStepInfo(TestStepFinished event, StepElement stepElement) {
        Result result = event.result;
        if (stepElement != null) {
            stepElement.setStatus(result.getStatus().name());
            stepElement.setDuration(result.getDuration());
            if (result.getErrorMessage() != null) {
                stepElement.setErrorMessage(result.getErrorMessage());
            }
        }
    }

    private ScenarioElement getCurrentScenarioElement(TestCase testCase){
        FeatureElement featureElement = getCurrentFeatureElement(testCase);
        Optional<ScenarioElement> scenarioElement = featureElement.getScenarios().stream()
                .filter(scenario -> scenario.getIndex().equals(scenarioIndexRead)).findFirst();
        return scenarioElement.orElse(null);
    }

    private StepElement getCurrentStepElement(ScenarioElement scenarioElement){
        if(scenarioElement != null) {
            return scenarioElement.getSteps().get(stepIndex);
        }
        return null;
    }

    private FeatureElement getCurrentFeatureElement(TestCase testCase){
        return _features.get(testCase.getUri());
    }

    private void finishTestReport(){
        _features.values().forEach(featureElement -> rootElement.appendChild(featureElement.toXMLElement(doc)));
        outputFile.write(doc);
    }
}