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
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HPEAlmOctaneGherkinFormatter implements Formatter, Reporter {
    private Document _doc = null;
    private Element _rootElement = null;
    private FileOutputStream _out;
    private ScenarioElement _currentScenario = null;
    private StepElement _currentStep = null;
    private FeatureElement _currentFeature = null;
    private List<StepElement> _backgroundSteps = null;
    private Integer _scenarioOutlineIndex = null;
    private List<String> cucumberFeatures;
    private ResourceLoader cucumberResourceLoader;
    private boolean throwException = false;

    public HPEAlmOctaneGherkinFormatter(ResourceLoader resourceLoader, List<String> features) {
        try {
            cucumberFeatures = features;
            cucumberResourceLoader = resourceLoader;
            _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            _rootElement = _doc.createElement(GherkinSerializer.ROOT_TAG_NAME);
            _doc.appendChild(_rootElement);
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {

    }

    @Override
    public void uri(String uri) {
        try {
            // Creating the node for the next feature to run
            if (_currentFeature == null) {
                _currentFeature = new FeatureElement();
            }
            throwException();
            addFeatureFileInfo(uri);
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void feature(Feature feature) {
        try {
            if (_currentFeature == null) {
                _currentFeature = new FeatureElement();
            }

            throwException();
            _currentFeature.setName(feature.getName());
            _currentFeature.setStarted(Instant.now().toEpochMilli());
            if(!feature.getTags().isEmpty()){
                String tag = feature.getTags().get(0).getName();
                if(tag.startsWith("@TID")){
                    _currentFeature.setTag(tag);
                }
            }
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        try {
            _scenarioOutlineIndex = 1;
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void examples(Examples examples) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void background(Background background) {
        try {
            _backgroundSteps = new ArrayList<>();
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void scenario(Scenario scenario) {
        try {
            throwException();
            if(isScenarioOutline(scenario)){
                _currentScenario = new ScenarioElement(scenario.getName(),_scenarioOutlineIndex++);
            } else {
                //this is a simple scenario
                _currentScenario = new ScenarioElement(scenario.getName());
                _scenarioOutlineIndex = null;
            }
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
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
        try {
            if(isScenarioOutlineStep()){
                // no need to keep generic steps - skip them
                return;
            }

            throwException();
            StepElement currentStep = new StepElement(step);
            if (_currentScenario != null) {
                _currentScenario.getSteps().add(currentStep);
            } else if (_backgroundSteps != null) {
                _backgroundSteps.add(currentStep);
            }
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        try {
            throwException();
            if(_currentScenario != null){
                _currentFeature.getScenarios().add(_currentScenario);
            }
            if (_backgroundSteps != null) {
                _currentFeature.getBackgroundSteps().addAll(_backgroundSteps);
            }
            _currentScenario = null;
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    private void addFeatureFileInfo(String featureFile) {
        Resource resource = findResource(featureFile);
        FeatureBuilder builder = new FeatureBuilder(new ArrayList<CucumberFeature>());
        if (resource != null) {
            _currentFeature.setPath(((FileResource) resource).getFile().getPath());
            _currentFeature.setFile(builder.read(resource));
        }
    }

    @Override
    public void before(Match match, Result result) {

    }

    @Override
    public void result(Result result) {
        try {
            throwException();
            if (_currentStep != null) {
                _currentStep.setStatus(result.getStatus());
                _currentStep.setDuration(result.getDuration());
                _currentStep = null;
            }
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void after(Match match, Result result) {

    }

    @Override
    public void match(Match match) {
        try {
            throwException();
            if (_currentScenario != null) {
                for (StepElement step : _currentScenario.getSteps()) {
                    // Checking if it's the same step
                    if (step.getLine() == ((StepDefinitionMatch) match).getStepLocation().getLineNumber()) {
                        _currentStep = step;
                    }
                }
            }
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void embedding(String s, byte[] bytes) {

    }

    @Override
    public void write(String s) {

    }

    private Resource findResource(String name){
        for (String featurePath : cucumberFeatures) {
            Iterable<Resource> resources = cucumberResourceLoader.resources(featurePath, ".feature");
            for (Resource resource: resources) {
                if(resource.getPath().contains(name.replace('/','\\'))){
                    return resource;
                }
            }
        }
        System.out.println("Resource file not found name:" + name);
        return null;
    }

    @Override
    public void done() {
        try {
            DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
            LSSerializer serializer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();
            _out = new FileOutputStream(GherkinSerializer.RESULTS_FILE_NAME);
            output.setByteStream(_out);
            serializer.write(_doc, output);
            //System.out.println(serializer.writeToString(_doc));
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            if (_out != null) {
                _out.close();
            }
        } catch (IOException e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void eof() {
        try {
            if (_currentFeature != null) {
                _rootElement.appendChild(_currentFeature.toXMLElement(_doc));

                _currentFeature = null;
                _currentScenario = null;
                _currentStep = null;
                _backgroundSteps = null;
            }
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    private void throwException(){
        if(throwException){
            throw new CucumberException("From HPEAlmOctaneGherkinFormatter");
        }
    }

    void setThrowException(boolean val){
        throwException = val;
    }

    String getXML() throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        String xml = "";
        try {
            DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
            LSSerializer serializer = impl.createLSSerializer();
            xml = serializer.writeToString(_doc);
        } catch (Exception e){
            //formatter must never throw an exception
            e.printStackTrace();
        }
        return xml;
    }
}
