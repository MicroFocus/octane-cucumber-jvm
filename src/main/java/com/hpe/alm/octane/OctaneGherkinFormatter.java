package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.*;
import cucumber.api.PickleStepTestStep;
import cucumber.api.Result;
import cucumber.api.TestCase;
import cucumber.api.event.EventListener;
import cucumber.api.event.*;
import gherkin.ast.ScenarioDefinition;
import gherkin.pickles.PickleTag;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;

public class OctaneGherkinFormatter implements EventListener {
    private OutputFile outputFile;

    private TestSourcesModel testSources = new TestSourcesModel();
    private TestTracker testTracker = new TestTracker();

    public OctaneGherkinFormatter(URL output) {
        outputFile = new OutputFile(output);
    }

    @Override
    public void setEventPublisher(EventPublisher publisher) {
        publisher.registerHandlerFor(TestSourceRead.class, this::handleSourceRead);

        publisher.registerHandlerFor(TestCaseStarted.class, this::handleCaseStarted);

        publisher.registerHandlerFor(TestStepStarted.class, this::handleStepStarted);

        publisher.registerHandlerFor(TestStepFinished.class, this::handleStepFinished);

        publisher.registerHandlerFor(TestRunFinished.class, this::handleRunFinished);
    }

    private void handleSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.uri, event);
    }

    private void handleCaseStarted(TestCaseStarted event) {
        if (testTracker.getCurrentFeature() == null || !testTracker.getCurrentFeature().getPath().equals(event.testCase.getUri())) {
            FeatureElement feature = new FeatureElement();
            feature.setPath(event.testCase.getUri());
            feature.setStarted(Instant.now().toEpochMilli());
            feature.setFileContent(testSources.getFeatureFileContent(event.testCase.getUri()));
            feature.setName(testSources.getFeatureName(event.testCase.getUri()));
            for (PickleTag tag : event.testCase.getTags()) {
                if (tag.getName().startsWith(Constants.TAG_ID)) {
                    feature.setTag(tag.getName());
                    break;
                }
            }
            testTracker.setCurrentFeature(feature);
        }

        testTracker.setCurrentScenario(getScenarioName(event.testCase));
    }

    private String getScenarioName(TestCase testCase){
        Optional<ScenarioDefinition> scn = testSources.getScenario(testCase.getUri(),testCase.getLine());
        if(scn.isPresent()){
            return scn.get().getName();
        }
        return testCase.getName();
    }

    private void handleStepStarted(TestStepStarted event) {
        if (event.testStep instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.testStep;
            String keyword = testSources.getKeywordFromSource(testTracker.getCurrentFeature().getPath(), testStep.getStepLine());
            String stepName = keyword + testStep.getStepText();

            testTracker.setCurrentStep(new StepElement(stepName));
        }
    }

    private void handleStepFinished(TestStepFinished event) {
        if (event.testStep instanceof PickleStepTestStep) {
            testTracker.getCurrentStep().setDuration(event.result.getDuration());
            testTracker.getCurrentStep().setStatus(getOctaneStatusFromResultStatus(event.result.getStatus()));
            if (event.result.getErrorMessage() != null) {
                testTracker.getCurrentStep().setErrorMessage(event.result.getErrorMessage());
            }
        }
    }

    private String getOctaneStatusFromResultStatus(Result.Type resultStatus){
        switch(resultStatus){
            case PENDING:
            case UNDEFINED:
            case AMBIGUOUS:
            case UNUSED:
               return Result.Type.SKIPPED.lowerCaseName();
            default:
                return resultStatus.lowerCaseName();
        }
    }

    private void handleRunFinished(TestRunFinished event) {
        Document doc = getDocument();
        Element rootElement = doc.createElement(GherkinSerializer.ROOT_TAG_NAME);
        rootElement.setAttribute("version", Constants.XML_VERSION);

        doc.appendChild(rootElement);
        testTracker.getFeatures().forEach(featureElement -> rootElement.appendChild(featureElement.toXMLElement(doc)));

        outputFile.write(doc);
    }

    private Document getDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            ErrorHandler.error("Failed to create result xml document.", e);
            return null;
        }
    }
}