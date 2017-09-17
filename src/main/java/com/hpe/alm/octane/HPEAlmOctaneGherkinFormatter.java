package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.*;
import cucumber.runtime.CucumberException;
import cucumber.runtime.FeatureBuilder;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.io.FileResource;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HPEAlmOctaneGherkinFormatter implements Formatter, Reporter {
    private Document _doc = null;
    private Element _rootElement = null;
    private OutputFile outputFile;
    private ScenarioElement _currentScenario = null;
    private StepElement _currentStep = null;
    private FeatureElement _currentFeature = null;
    private List<StepElement> _backgroundSteps = null;
    private Integer _scenarioOutlineIndex = null;
    private List<String> cucumberFeatures;
    private ResourceLoader cucumberResourceLoader;

    public HPEAlmOctaneGherkinFormatter(ResourceLoader resourceLoader, List<String> features, OutputFile outputFile) {
        cucumberFeatures = features;
        cucumberResourceLoader = resourceLoader;
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
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {}

    @Override
    public void uri(String uri) {
        setCurrentFeature();
        addFeatureFileInfo(uri);
    }

    @Override
    public void feature(Feature feature) {
        setCurrentFeature();
        _currentFeature.setName(feature.getName());
        _currentFeature.setStarted(Instant.now().toEpochMilli());
        if(!feature.getTags().isEmpty()){
            String tag = feature.getTags().get(0).getName();
            if(tag.startsWith("@TID")){
                _currentFeature.setTag(tag);
            }
        }
    }

    private void setCurrentFeature(){
        // Creating the node for the next feature to run
        if (_currentFeature == null) {
            _currentFeature = new FeatureElement();
        }
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        _scenarioOutlineIndex = 1;
    }

    @Override
    public void examples(Examples examples) {}

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {}

    @Override
    public void background(Background background) {
        _backgroundSteps = new ArrayList<>();
    }

    @Override
    public void scenario(Scenario scenario) {
        if(isScenarioOutline(scenario)){
            _currentScenario = new ScenarioElement(scenario.getName(),_scenarioOutlineIndex++);
        } else {
            //this is a simple scenario
            _currentScenario = new ScenarioElement(scenario.getName());
            _scenarioOutlineIndex = null;
        }
    }

    private boolean isScenarioOutline(Scenario scenario){
        return _scenarioOutlineIndex != null && scenario.getKeyword().compareTo("Scenario Outline") == 0;
    }

    private boolean isScenarioOutlineStep(){
        if(_scenarioOutlineIndex!=null && _scenarioOutlineIndex == 1){
            return true;
        }
        return false;
    }

    @Override
    public void step(Step step) {
        if(isScenarioOutlineStep()){
            // no need to keep generic steps - skip them
            return;
        }

        StepElement currentStep = new StepElement(step);
        if (_currentScenario != null) {
            _currentScenario.getSteps().add(currentStep);
        } else if (_backgroundSteps != null) {
            currentStep.setBackgroundStep();
            _backgroundSteps.add(currentStep);
        }
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        if(_currentScenario != null){
            _currentFeature.getScenarios().add(_currentScenario);
        }

        if (_backgroundSteps != null && _currentFeature.getBackgroundSteps().size()==0) {
            _currentFeature.getBackgroundSteps().addAll(_backgroundSteps);
        }
        _currentScenario = null;
    }

    private void addFeatureFileInfo(String featureFile) {
        try {
            Resource resource = findResource(featureFile);
            FeatureBuilder builder = new FeatureBuilder(new ArrayList<CucumberFeature>());
            _currentFeature.setPath(((FileResource) resource).getFile().getPath());
            _currentFeature.setFile(builder.read(resource));
        } catch (Exception e) {
            throw new CucumberException(Constants.errorPrefix + "Failed to find feature file:" + featureFile ,e);
        }
    }

    @Override
    public void before(Match match, Result result) {}

    @Override
    public void result(Result result) {
        if (_currentStep != null) {
            _currentStep.setStatus(result.getStatus());
            _currentStep.setDuration(result.getDuration());
            if(result.getErrorMessage()!=null){
                _currentStep.setErrorMessage(result.getErrorMessage());
            }
            _currentStep = null;
        }
    }

    @Override
    public void after(Match match, Result result) {}

    @Override
    public void match(Match match) {
        if(match == Match.UNDEFINED){
            _currentStep = null;
            return;
        }

        if (_currentScenario != null) {
            setCurrentStep(_currentScenario.getSteps(), (StepDefinitionMatch)match);
        } else if (_backgroundSteps != null) {
            setCurrentStep(_backgroundSteps, (StepDefinitionMatch)match);
        }
    }

    private void setCurrentStep(List<StepElement> steps, StepDefinitionMatch match) {
        for (StepElement step : steps) {
            // Checking if it's the same step
            if (step.getLine() == match.getStepLocation().getLineNumber()) {
                _currentStep = step;
            }
        }
    }

    @Override
    public void embedding(String s, byte[] bytes) {}

    @Override
    public void write(String s) {}

    private Resource findResource(String name){
        ArrayList<String> paths = new ArrayList<>();
        for (String featurePath : cucumberFeatures) {
            Iterable<Resource> resources = cucumberResourceLoader.resources(featurePath, ".feature");
            for (Resource resource: resources) {
                String path = resource.getPath();
                if(path.contains(name.replace('/', File.separatorChar))){
                    return resource;
                }
                paths.add(path);
            }
        }
        System.out.println("Resource file not found name:" + name);
        System.out.println("File.separatorChar = " + File.separatorChar);
        for (String path : paths) {
            System.out.println("path = " + path);
        }
        return null;
    }

    @Override
    public void done() {
        outputFile.write(_doc);
    }

    @Override
    public void close() {
    }

    @Override
    public void eof() {
        if (_currentFeature != null) {
            _rootElement.appendChild(_currentFeature.toXMLElement(_doc));

            _currentFeature = null;
            _currentScenario = null;
            _currentStep = null;
            _backgroundSteps = null;
        }
    }

    String getXML() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
        DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
        LSSerializer serializer = impl.createLSSerializer();
        return serializer.writeToString(_doc);
    }
}
