package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.*;
import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.EventListener;
import cucumber.api.event.*;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.FileResource;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.util.*;

//todo - need to check it with other versions
public class OctaneGherkinFormatter implements EventListener {
    private Document doc;
    private Element rootElement;
    private OutputFile outputFile;
    private Map<String, FeatureElement> featuresMap = new LinkedHashMap<>();
    private Integer scenarioIndexRead = 0;
    private Integer scenarioIndexWrite = 0;
    private Integer stepIndex = 0;
    private Map<String, GherkinDocument> cucumberFeatures = new HashMap<>();
    private ResourceLoader resourceLoader;

    OctaneGherkinFormatter(ResourceLoader resourceLoader, List<CucumberFeature> cucumberFeatures, OutputFile outputFile){
        initFeatures(cucumberFeatures);
        this.resourceLoader = resourceLoader;
        this.outputFile = outputFile;
        try {
            doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            rootElement = doc.createElement(GherkinSerializer.ROOT_TAG_NAME);
            rootElement.setAttribute("version", Constants.XML_VERSION);
            doc.appendChild(rootElement);
        } catch (ParserConfigurationException e) {
            throw new CucumberException(Constants.errorPrefix + "Failed to create xml document", e);
        }
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::readFeatureFile);
        publisher.registerHandlerFor(TestCaseFinished.class, this::scenarioFinished);
        publisher.registerHandlerFor(TestStepFinished.class, this::testStepFinished);
        publisher.registerHandlerFor(TestRunFinished.class, event -> finishTestReport());
    }

    private void initFeatures(List<CucumberFeature> features){
        features.forEach(cucumberFeature -> cucumberFeatures.put(cucumberFeature.getUri(), cucumberFeature.getGherkinFeature()));
    }

    void readFeatureFile(TestSourceRead event) {
        Feature feature = getCurrentFeature(event.uri);
        FeatureElement featureElement = new FeatureElement();
        setScenariosInfo(feature.getChildren(), featureElement, new ArrayList<>());
        setFeatureInfo(event, feature, featureElement);
        featuresMap.put(event.uri, featureElement);
    }

    private Feature getCurrentFeature(String uri) {
        return cucumberFeatures.get(uri).getFeature();
    }

    private void setScenariosInfo(List<ScenarioDefinition> scenarioDefinitions, FeatureElement featureElement, List<StepElement> backgroundSteps){
        Integer scenarioOutlineIndex = 1;
        for(ScenarioDefinition scenarioDefinition : scenarioDefinitions) {
            if(isScenarioBackground(scenarioDefinition)) {
                setBackgroundScenarioInfo(featureElement, scenarioDefinition, backgroundSteps);
            } else if(isScenarioOutline(scenarioDefinition)) {
                setScenarioOutlineInfo(featureElement, scenarioDefinition, scenarioOutlineIndex);
            } else {
                setRegularScenarioInfo(featureElement, scenarioDefinition);
            }
        }
        addBackgroundStepsToScenarios(featureElement, backgroundSteps);
    }

    private void setBackgroundScenarioInfo(FeatureElement featureElement, ScenarioDefinition scenarioDefinition, List<StepElement> backgroundSteps) {
        scenarioDefinition.getSteps().forEach(step -> {
            StepElement stepElement = new StepElement(step);
            stepElement.setBackgroundStep();
            backgroundSteps.add(stepElement);
        });
        featureElement.getBackgroundSteps().addAll(backgroundSteps);
    }

    private void setScenarioOutlineInfo(FeatureElement featureElement, ScenarioDefinition scenarioDefinition, Integer scenarioOutlineIndex) {
        ScenarioOutline scenarioOutline = (ScenarioOutline) scenarioDefinition;
        for (Examples examples : scenarioOutline.getExamples())
        {
            for (TableRow ignored : examples.getTableBody())
            {
                ScenarioElement scenarioElement = new ScenarioElement(scenarioIndexWrite++, scenarioDefinition.getName(), scenarioDefinition.getKeyword(), scenarioOutlineIndex++);
                addStepsToScenario(scenarioDefinition, scenarioElement);
                featureElement.getScenarios().add(scenarioElement);
            }
        }
    }

    private void setRegularScenarioInfo(FeatureElement featureElement, ScenarioDefinition scenarioDefinition) {
        ScenarioElement scenarioElement = new ScenarioElement(scenarioIndexWrite++, scenarioDefinition.getName(), scenarioDefinition.getKeyword());
        addStepsToScenario(scenarioDefinition, scenarioElement);
        featureElement.getScenarios().add(scenarioElement);
    }

    private void addStepsToScenario(ScenarioDefinition scenarioDefinition, ScenarioElement scenarioElement) {
        List<StepElement> steps = new ArrayList<>();
        scenarioDefinition.getSteps().forEach(step -> {
            StepElement stepElement = new StepElement(step);
            steps.add(stepElement);
        });
        scenarioElement.getSteps().addAll(steps);
    }

    private void addBackgroundStepsToScenarios(FeatureElement featureElement, List<StepElement> backgroundSteps) {
        if(backgroundSteps.size() > 0) {
            featureElement.getScenarios().forEach(scenarioElement ->
                    scenarioElement.getSteps().addAll(0, backgroundSteps));
        }
    }

    private boolean isScenarioBackground(ScenarioDefinition scenarioDefinition){
        return scenarioDefinition.getKeyword().equals(ScenarioElement.ScenarioType.BACKGROUND.getScenarioType());
    }

    private boolean isScenarioOutline(ScenarioDefinition scenarioDefinition){
        return scenarioDefinition.getKeyword().equals(ScenarioElement.ScenarioType.OUTLINE.getScenarioType());
    }

    private void setFeatureInfo(TestSourceRead event, Feature feature, FeatureElement featureElement) {
        featureElement.setName(feature.getName());
        featureElement.setPath(getResourcePath(event.uri, event.uri));
        featureElement.setFileContent(event.source);
        featureElement.setStarted(event.getTimeStamp());

        for(Tag tag : feature.getTags()) {
            if(tag.getName().startsWith("@TID")) {
                featureElement.setTag(tag.getName());
                break;
            }
        }
    }

    private String getResourcePath(String originalUri, String uri) {
        Iterable<Resource> resources = resourceLoader.resources(uri, ".feature");
        String path = "";
        try {
            for (Resource resource: resources) {
                path = ((FileResource) resource).getFile().getPath();
                if(path.contains(originalUri.replace('/', File.separatorChar))){
                    return path;
                }
            }
        }
        catch (IllegalArgumentException originalException) {
            return getResourcePath(uri, MultiLoader.CLASSPATH_SCHEME + uri);
        }

        System.out.println("Resource file not found:" + originalUri);
        return path;
    }

    private String getResourceFromClasspath(String uri, IllegalArgumentException originalException) {
        String path;
//        if (!uri.startsWith(MultiLoader.CLASSPATH_SCHEME) &&
//                originalException.getMessage().contains("Not a file or directory")) {
        try {
            path = getResourcePath(uri, MultiLoader.CLASSPATH_SCHEME + uri);
        } catch (IllegalArgumentException secondException) {
            throw secondException;

        }
//        }
//        else {
//            throw originalException;
//        }
        return path;
    }

    /**************** Scenario Finished ******************/
    void scenarioFinished(TestCaseFinished event) {
        ScenarioElement scenarioElement = getCurrentScenarioElement(event.getTestCase());
        if(scenarioElement != null) {
            scenarioElement.getSteps().removeAll(getCurrentFeatureElement(event.getTestCase()).getBackgroundSteps());
        }
        stepIndex = 0;
        scenarioIndexRead++;
    }

    /**************** Test Step Finished ******************/
    void testStepFinished(TestStepFinished event) {
        ScenarioElement scenarioElement = getCurrentScenarioElement(event.getTestCase());
        if(scenarioElement != null) {
            StepElement stepElement = getCurrentStepElement(scenarioElement);
            if(stepElement != null) {
                setStepInfo(event, stepElement);
                if (scenarioElement.isScenarioOutline()) {
                    PickleStepTestStep testStep = (PickleStepTestStep) (event.testStep);
                    stepElement.setName(stepElement.getKeyword() + testStep.getStepText());
                }
                stepIndex++;
            }
        }
    }

    private void setStepInfo(TestStepFinished event, StepElement stepElement) {
        Result result = event.result;
        if (stepElement != null) {
            stepElement.setStatus(result.getStatus().name());
            stepElement.setDuration(result.getDuration());
            if (result.getError() != null) {
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
        return scenarioElement.getSteps().get(stepIndex);
    }

    private FeatureElement getCurrentFeatureElement(TestCase testCase){
        return featuresMap.get(testCase.getUri());
    }

    void finishTestReport(){
        featuresMap.values().forEach(featureElement -> rootElement.appendChild(featureElement.toXMLElement(doc)));
        outputFile.write(doc);
    }

    String getXML() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
        LSSerializer serializer = impl.createLSSerializer();
        return serializer.writeToString(doc);
    }
}