import cucumber.runtime.StepDefinitionMatch;
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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class HPEAlmOctaneGherkinFormatter implements Formatter, Reporter {

    private static final String RESULTS_FILE_NAME = "gherkinNGAResults.xml_";
    private static final String ROOT_TAG_NAME = "features";
    private static final String FEATURE_TAG_NAME = "feature";
    private static final String SCENARIO_TAG_NAME = "scenario";
    private static final String SCENARIOS_TAG_NAME = "scenarios";
    private static final String FILE_TAG_NAME = "file";
    private static final String STEP_TAG_NAME = "step";
    private static final String STEPS_TAG_NAME = "steps";
    private static final String BACKGROUND_TAG_NAME = "background";

    private Document _doc = null;
    private Element _rootElement = null;
    private FileOutputStream _out;
    private ScenarioElement _currentScenario = null;
    private StepElement _currentStep = null;
    private FeatureElement _currentFeature = null;
    private List<StepElement> _backgroundSteps = null;
    private File featureFile = null;
    private Integer _scenarioOutlineIndex = null;

    public HPEAlmOctaneGherkinFormatter() {
        try {
            _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            _rootElement = _doc.createElement(ROOT_TAG_NAME);
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

            appendFeatureFile(uri);
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

            _currentFeature.setName(feature.getName());
            _currentFeature.setStarted(Instant.now().toEpochMilli());
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
            if(_scenarioOutlineIndex!=null && scenario.getKeyword().compareTo("Scenario Outline")==0){
                //scenario outline
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

    @Override
    public void step(Step step) {
        try {
            if(_scenarioOutlineIndex!=null && _scenarioOutlineIndex == 1){
                //inside the generic scenario outline
                // no need to keep generic steps - skip them
                return;
            }

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
            _currentFeature.getScenarios().add(_currentScenario);
            if (_backgroundSteps != null) {
                _currentFeature.getBackgroundSteps().addAll(_backgroundSteps);
            }
            _currentScenario = null;
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    private void appendFeatureFile(String featureFile) {
        try {
            this.featureFile = new FileFinder().findFile(featureFile);
            if (this.featureFile == null) {
                return;
            }
            byte[] encoded = Files.readAllBytes(this.featureFile.toPath());
            String fileStr = new String(encoded, StandardCharsets.UTF_8);
            _currentFeature.setPath(this.featureFile.getPath());
            _currentFeature.setFile(fileStr);
        } catch (Exception e) {
            //formatter must never throw an exception
            e.printStackTrace();
        }
    }

    @Override
    public void before(Match match, Result result) {

    }

    @Override
    public void result(Result result) {
        try {
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
            if (_currentScenario != null) {
                for (StepElement step : _currentScenario._steps) {
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

    private class FileFinder {
        private ArrayList<File> _resultFiles = new ArrayList<>();

        File findFile(String fileName) {
            String[] features = OctaneCucumber.getFeatures();
            if(features == null || features.length==0){
                System.out.println("OctaneCucumber.features was not found.\nVerify that you are running you tests with the \"@RunWith(OctaneCucumber.class)\" annotation\nNGA report will not be generated.");
                return null;
            }

            for(String root : features){
                File file = findFile(new File(root), fileName);
                if(file != null){
                    return file;
                }
            }
            System.out.println("File " + fileName + " was not found. NGA report will not be generated.");
            return null;
        }

        File findFile(File rootDir, String fileName) {
            find(rootDir, fileName);
            if (_resultFiles.size() == 1) {
                return _resultFiles.get(0);
            } else if (_resultFiles.size() > 1) {
                return getFileToUseOutOfMultipleResults(_resultFiles);
            } else {
                System.out.println("Feature file was not found. NGA report will not be generated.");
                return null;
            }
        }

        private void find(File rootDir, String fileName) {
            if (rootDir == null) {
                return;
            }

            File[] files = rootDir.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        find(file, fileName);
                    } else if (file.getAbsolutePath().endsWith(fileName) || file.getAbsolutePath().endsWith(fileName.replace('/', File.separatorChar))) {
                        _resultFiles.add(file);
                    }
                }
            }
        }

        private File getFileToUseOutOfMultipleResults(ArrayList<File> files) {
            if (files == null || files.isEmpty()) {
                return null;
            }

            //If we have a file that has "src" folder in it's path - return it.
            for (File file : files) {
                if (file.getAbsolutePath().contains(File.separator + "src" + File.separator)) {
                    return file;
                }
            }

            //else - return the first one we found.
            return files.get(0);
        }
    }

    @Override
    public void done() {
        try {
            if (featureFile == null) {
                // feature file was not found -> do not generate report
                return;
            }
            DOMImplementationRegistry reg = DOMImplementationRegistry.newInstance();
            DOMImplementationLS impl = (DOMImplementationLS) reg.getDOMImplementation("LS");
            LSSerializer serializer = impl.createLSSerializer();
            LSOutput output = impl.createLSOutput();
            _out = new FileOutputStream(RESULTS_FILE_NAME);
            output.setByteStream(_out);
            serializer.write(_doc, output);
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
                _rootElement.appendChild(_currentFeature.toXMLElement());

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

    interface GherkinSerializer {
        Element toXMLElement();
    }

    class FeatureElement implements GherkinSerializer {
        private String _name;
        private String _path;
        private String _file;
        private Long _started;
        private List<ScenarioElement> _scenarios;
        private List<StepElement> _backgroundSteps;

        FeatureElement() {
            _scenarios = new ArrayList<>();
            _backgroundSteps = new ArrayList<>();
        }

        List<ScenarioElement> getScenarios() {
            return _scenarios;
        }

        List<StepElement> getBackgroundSteps() {
            return _backgroundSteps;
        }

        void setName(String name) {
            this._name = name;
        }

        void setPath(String path) {
            this._path = path;
        }

        void setFile(String file) {
            this._file = file;
        }

        void setStarted(Long started) { this._started = started; }

        public Element toXMLElement() {
            Element feature = _doc.createElement(FEATURE_TAG_NAME);

            // Adding the feature members
            feature.setAttribute("name", _name);
            feature.setAttribute("path", _path);
            if (_started != null) {
                feature.setAttribute("started", _started.toString());
            }

            // Adding the file to the feature
            Element fileElement = _doc.createElement(FILE_TAG_NAME);
            fileElement.appendChild(_doc.createCDATASection(_file));
            feature.appendChild(fileElement);

            Element scenariosElement = _doc.createElement(SCENARIOS_TAG_NAME);

            // Serializing the background
            if (_backgroundSteps != null && _backgroundSteps.size()>0) {
                Element backgroundElement = _doc.createElement(BACKGROUND_TAG_NAME);
                Element backgroundStepsElement = _doc.createElement(STEPS_TAG_NAME);
                backgroundElement.appendChild(backgroundStepsElement);

                for (StepElement step : _backgroundSteps) {
                    backgroundStepsElement.appendChild(step.toXMLElement());
                }

                backgroundElement.appendChild(backgroundStepsElement);
                scenariosElement.appendChild(backgroundElement);
            }

            // Serializing the scenarios
            for (ScenarioElement scenario : _scenarios) {
                scenariosElement.appendChild(scenario.toXMLElement());
            }

            feature.appendChild(scenariosElement);

            return feature;
        }
    }

    class ScenarioElement implements GherkinSerializer {
        private String _name;
        private List<StepElement> _steps;
        private Integer _outlineIndex = 0;

        ScenarioElement(String name, int outlineIndex) {
            this(name);
            _outlineIndex = outlineIndex;
        }

        ScenarioElement(String name) {
            _name = name;
            _steps = new ArrayList<>();
        }

        List<StepElement> getSteps() {
            return _steps;
        }

        public Element toXMLElement() {
            // Adding the feature members
            Element scenario = _doc.createElement(SCENARIO_TAG_NAME);
            scenario.setAttribute("name", _name);
            if(_outlineIndex>0){
                scenario.setAttribute("outlineIndex", _outlineIndex.toString());
            }

            // Serializing the steps
            Element steps = _doc.createElement(STEPS_TAG_NAME);
            for (StepElement step : _steps) {
                steps.appendChild(step.toXMLElement());
            }

            scenario.appendChild(steps);

            return scenario;
        }
    }

    class StepElement implements GherkinSerializer {
        private String _name;
        private String _status;
        private Integer _line;
        private Long _duration;

        StepElement(Step step) {
            _name = step.getKeyword() + step.getName();
            _line = step.getLine();
        }

        void setStatus(String status) {
            this._status = status;
        }

        void setDuration(Long duration) {
            this._duration = duration;
        }


        Integer getLine() {
            return _line;
        }

        public Element toXMLElement() {
            Element step = _doc.createElement(STEP_TAG_NAME);

            step.setAttribute("name", _name);
            if (_status != null && !_status.isEmpty()) {
                step.setAttribute("status", _status);
            }

            String duration = _duration != null ? _duration.toString() : "0";
            step.setAttribute("duration", duration);

            return step;
        }
    }
}
