package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.*;
import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.io.FileResource;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
//import java.io.File;
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//
//public class HPEAlmOctaneGherkinFormatter {
//    private Document _doc = null;
//    private Element _rootElement = null;
//    private OutputFile outputFile;
//    private ScenarioElement _currentScenario = null;
//    private StepElement _currentStep = null;
//    private FeatureElement _currentFeature = null;
//    private List<StepElement> _backgroundSteps = null;
//    private Integer _scenarioOutlineIndex = null;
//    private List<String> cucumberFeatures;
//    private ResourceLoader cucumberResourceLoader;
//
//    public HPEAlmOctaneGherkinFormatter(ResourceLoader resourceLoader, List<String> features, OutputFile outputFile) {
//        cucumberFeatures = features;
//        cucumberResourceLoader = resourceLoader;
//        this.outputFile = outputFile;
//        try {
//            _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
//            _rootElement = _doc.createElement(GherkinSerializer.ROOT_TAG_NAME);
//            _rootElement.setAttribute("version",Constants.XML_VERSION);
//            _doc.appendChild(_rootElement);
//        } catch (ParserConfigurationException e) {
//            throw new CucumberException(Constants.errorPrefix + "Failed to create xml document",e);
//        }
//    }
//
//    public void scenarioOutline(ScenarioOutline scenarioOutline) {
//        _scenarioOutlineIndex = 1;
//    }
//
//    public void background(Background background) {
//        _backgroundSteps = new ArrayList<>();
//    }
//
//    public void step(Step step) {
//        if(isScenarioOutlineStep()){
//            // no need to keep generic steps - skip them
//            return;
//        }
//
//        StepElement currentStep = new StepElement(step);
//        if (_currentScenario != null) {
//            _currentScenario.getSteps().add(currentStep);
//        } else if (_backgroundSteps != null) {
//            currentStep.setBackgroundStep();
//            _backgroundSteps.add(currentStep);
//        }
//    }
//
//    public void result(Result result) {
//        if (_currentStep != null) {
//            _currentStep.setStatus(result.getStatus());
//            _currentStep.setDuration(result.getDuration());
//            if(result.getErrorMessage()!=null){
//                _currentStep.setErrorMessage(result.getErrorMessage());
//            }
//            _currentStep = null;
//        }
//    }
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
//

//    public void eof() {
//        if (_currentFeature != null) {
//            _rootElement.appendChild(_currentFeature.toXMLElement(_doc));
//
//            _currentFeature = null;
//            _currentScenario = null;
//            _currentStep = null;
//            _backgroundSteps = null;
//        }
//    }
//
//    String getXML() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
//        DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
//        DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
//        LSSerializer serializer = impl.createLSSerializer();
//        return serializer.writeToString(_doc);
//    }
//}
