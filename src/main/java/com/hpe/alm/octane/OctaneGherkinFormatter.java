package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.*;
import com.hpe.alm.octane.infra.ErrorHandler;
import com.hpe.alm.octane.infra.GherkinSerializer;
import io.cucumber.plugin.EventListener;
import io.cucumber.plugin.event.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.net.URL;
import java.time.Instant;

import static io.cucumber.plugin.event.Status.*;

public class OctaneGherkinFormatter implements EventListener {

    private OutputFile outputFile;

    private TestSourcesModel testSources = new TestSourcesModel();
    private TestTracker testTracker = new TestTracker();

    public OctaneGherkinFormatter(URL output) {
        outputFile = new OutputFile(output);
    }

    @Override
    public void setEventPublisher(EventPublisher eventPublisher) {
        eventPublisher.registerHandlerFor(TestSourceRead.class, this::handleSourceRead);

        eventPublisher.registerHandlerFor(TestCaseStarted.class, this::handleCaseStarted);

        eventPublisher.registerHandlerFor(TestStepStarted.class, this::handleStepStarted);

        eventPublisher.registerHandlerFor(TestStepFinished.class, this::handleStepFinished);

        eventPublisher.registerHandlerFor(TestRunFinished.class, this::handleRunFinished);
    }

    private void handleSourceRead(TestSourceRead event) {
        testSources.addTestSourceReadEvent(event.getUri().toString(), event);
    }

    private void handleCaseStarted(TestCaseStarted event) {
        if (testTracker.getCurrentFeature() == null || !testTracker.getCurrentFeature().getPath().equals(event.getTestCase().getUri().toString())) {
            FeatureElement feature = new FeatureElement();
            feature.setPath(event.getTestCase().getUri().toString());
            feature.setStarted(Instant.now().toEpochMilli());
            feature.setFileContent(testSources.getFeatureFileContent(event.getTestCase().getUri().toString()));
            feature.setName(testSources.getFeatureName(event.getTestCase().getUri().toString()));
            for (String tag : event.getTestCase().getTags()) {
                if (tag.startsWith(Constants.TAG_ID)) {
                    feature.setTag(tag);
                    break;
                }
            }
            testTracker.setCurrentFeature(feature);
        }

        testTracker.setCurrentScenario(testSources.getScenarioName(event.getTestCase()));
    }

    private void handleStepStarted(TestStepStarted event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            PickleStepTestStep testStep = (PickleStepTestStep) event.getTestStep();
            String keyword = testSources.getKeywordFromSource(testTracker.getCurrentFeature().getPath(), testStep.getStepLine());
            String stepName = keyword + testStep.getStepText();
            testTracker.setCurrentStep(new StepElement(stepName));
        }
    }

    private void handleStepFinished(TestStepFinished event) {
        if (event.getTestStep() instanceof PickleStepTestStep) {
            testTracker.getCurrentStep().setDuration(event.getResult().getDuration().toMillis());
            testTracker.getCurrentStep().setStatus(getOctaneStatusFromResultStatus(event.getResult().getStatus()));
            if (event.getResult().getError() != null) {
                testTracker.getCurrentStep().setErrorMessage(event.getResult().getError());
            }
        }
    }

    private String getOctaneStatusFromResultStatus(Status resultStatus){
        switch(resultStatus){
            case PENDING:
            case UNDEFINED:
            case AMBIGUOUS:
            case UNUSED:
                return SKIPPED.name().toLowerCase();
            default:
                return resultStatus.name().toLowerCase();
        }
    }

    private void handleRunFinished(TestRunFinished event) {
        Document resultXmlDoc = getDocument();
        Element rootElement = resultXmlDoc.createElement(GherkinSerializer.ROOT_TAG_NAME);
        rootElement.setAttribute("version", Constants.XML_VERSION);

        resultXmlDoc.appendChild(rootElement);
        testTracker.getFeatures().forEach(featureElement -> rootElement.appendChild(featureElement.toXMLElement(resultXmlDoc)));

        outputFile.write(resultXmlDoc);
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