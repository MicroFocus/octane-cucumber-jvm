import cucumber.runtime.CucumberException;
import cucumber.runtime.StepDefinitionMatch;
import gherkin.formatter.Formatter;
import gherkin.formatter.Reporter;
import gherkin.formatter.model.*;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class GherkinNGAFormatter implements Formatter, Reporter {

    public static final String RESULTS_FILE_NAME = "gherkinNGAResults.xml_";
    public static final String ROOT_TAG_NAME = "features";
    public static final String FEATURE_TAG_NAME = "feature";
    public static final String SCENARIO_TAG_NAME = "scenario";
    public static final String SCENARIOS_TAG_NAME = "scenarios";
    public static final String FILE_TAG_NAME = "file";
    public static final String STEP_TAG_NAME = "step";
    public static final String STEPS_TAG_NAME = "steps";
    public static final String BACKGROUND_TAG_NAME = "background";
    public static final String WORKSPACE_DIR_NAME = "test-classes";

    private Document _doc = null;
    private Element _rootElement = null;
    private Writer _out;
    private ScenarioElement _currentScenario = null;
    private StepElement _currentStep = null;
    private FeatureElement _currentFeature = null;
    private List<StepElement> _backgroundSteps = null;

    public GherkinNGAFormatter() {
        try {
            _doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            _rootElement = _doc.createElement(ROOT_TAG_NAME);
            _doc.appendChild(_rootElement);
            _out = new FileWriter(RESULTS_FILE_NAME);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void syntaxError(String s, String s1, List<String> list, String s2, Integer integer) {

    }

    @Override
    public void uri(String uri) {
        // Creating the node for the next feature to run
        if (_currentFeature == null) {
            _currentFeature = new FeatureElement();
        }

        appendFeatureFile(uri);
    }

    @Override
    public void feature(Feature feature) {
        if (_currentFeature == null) {
            _currentFeature = new FeatureElement();
        }

        _currentFeature.setName(feature.getName());
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {

    }

    @Override
    public void examples(Examples examples) {

    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {

    }

    @Override
    public void background(Background background) {
        _backgroundSteps = new ArrayList<StepElement>();
    }

    @Override
    public void scenario(Scenario scenario) {
        _currentScenario = new ScenarioElement(scenario.getName());
    }

    @Override
    public void step(Step step) {
        StepElement currentStep = new StepElement(step);
        if (_currentScenario != null) {
            _currentScenario.getSteps().add(currentStep);
        } else if (_backgroundSteps != null) {
            _backgroundSteps.add(currentStep);
        }
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        _currentFeature.getScenarios().add(_currentScenario);
        _currentScenario = null;
    }

    private void appendFeatureFile(String featureFileSubPath) {
        try {
            //Get current runtime location
            URL url = GherkinNGAFormatter.class.getResource(GherkinNGAFormatter.class.getSimpleName() + ".class");
            File classFile = new File(url.toURI());
            File workspaceDir = getWorkspaceDirFromPath(classFile);
            //get the .feature file
            File featureFile = new FileFinder().findFile(workspaceDir, featureFileSubPath);
            byte[] encoded = Files.readAllBytes(featureFile.toPath());
            String fileStr = new String(encoded, StandardCharsets.UTF_8);
            _currentFeature.setPath(featureFile.getPath().substring(workspaceDir.getPath().length() + 1));
            _currentFeature.setFile(fileStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void before(Match match, Result result) {

    }

    @Override
    public void result(Result result) {
        if (_currentStep != null) {
            _currentStep.setStatus(result.getStatus());
            _currentStep.setDuration(result.getDuration());
            _currentStep = null;
        }
    }

    @Override
    public void after(Match match, Result result) {

    }

    @Override
    public void match(Match match) {
        if (_currentScenario != null) {
            for (StepElement step : _currentScenario._steps) {
                // Checking if it's the same step
                if (step.getLine() == ((StepDefinitionMatch) match).getStepLocation().getLineNumber()) {
                    _currentStep = step;
                }
            }
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

        public File findFile(File rootDir, String fileName) {
            find(rootDir, fileName);
            if (_resultFiles.size() == 1) {
                return _resultFiles.get(0);
            } else if (_resultFiles.size() > 1) {
                return getFileToUseOutOfMultipleResults(_resultFiles);
            } else {
                throw new CucumberException("Didn't find the feature file " + fileName + " in the path: " + rootDir);
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

    private static File getWorkspaceDirFromPath(File file) {

        while (file != null && !file.getAbsolutePath().endsWith(WORKSPACE_DIR_NAME)) {
            try {
                PrintWriter printWriter = new PrintWriter("C:/Hanan1.txt", "utf-8");
                printWriter.print(file.getParentFile());
                printWriter.close();
                file = file.getParentFile();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    @Override
    public void done() {
        try {
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.INDENT, "yes");
            StreamResult result = new StreamResult(_out);
            DOMSource source = new DOMSource(_doc);
            trans.transform(source, result);
        } catch (Exception e) {
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
            e.printStackTrace();
        }
    }

    @Override
    public void eof() {
        if (_currentFeature != null) {
            _rootElement.appendChild(_currentFeature.toXMLElement());

            _currentFeature = null;
            _currentScenario = null;
            _currentStep = null;
            _backgroundSteps = null;
        }
    }

    interface GherkinSerializer {
        Element toXMLElement();
    }

    private class FeatureElement implements GherkinSerializer {
        private String _name;
        private String _path;
        private String _file;
        private List<ScenarioElement> _scenarios;

        FeatureElement() {
            _scenarios = new ArrayList<ScenarioElement>();
        }

        public List<ScenarioElement> getScenarios() {
            return _scenarios;
        }

        public void setName(String name) {
            this._name = name;
        }

        public void setPath(String path) {
            this._path = path;
        }

        public void setFile(String file) {
            this._file = file;
        }

        public Element toXMLElement() {
            Element feature = _doc.createElement(FEATURE_TAG_NAME);

            // Adding the feature members
            feature.setAttribute("name", _currentFeature._name);
            feature.setAttribute("path", _currentFeature._path);

            // Adding the file to the feature
            Element fileElement = _doc.createElement(FILE_TAG_NAME);
            fileElement.appendChild(_doc.createCDATASection(_currentFeature._file));
            feature.appendChild(fileElement);

            Element scenariosElement = _doc.createElement(SCENARIOS_TAG_NAME);

            // Serializing the background
            if (_backgroundSteps != null) {
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

    private class ScenarioElement implements GherkinSerializer {
        private String _name;
        private List<StepElement> _steps;
        private List<StepElement> _backgroundSteps;

        ScenarioElement(String name) {
            _name = name;
            _steps = new ArrayList<StepElement>();
        }

        public List<StepElement> getSteps() {
            return _steps;
        }

        public Element toXMLElement() {
            // Adding the feature members
            Element scenario = _doc.createElement(SCENARIO_TAG_NAME);
            scenario.setAttribute("name", _name);

            // Serializing the steps
            Element steps = _doc.createElement(STEPS_TAG_NAME);
            for (StepElement step : _steps) {
                steps.appendChild(step.toXMLElement());
            }

            scenario.appendChild(steps);

            return scenario;
        }
    }

    private class StepElement implements GherkinSerializer {
        private String _name;
        private String _status;
        private Integer _line;
        private Long _duration;

        StepElement(Step step) {
            _name = step.getKeyword() + step.getName();
            _line = step.getLine();
        }

        public void setStatus(String status) {
            this._status = status;
        }

        public void setDuration(Long duration) {
            this._duration = duration;
        }


        public Integer getLine() {
            return _line;
        }

        public Element toXMLElement() {
            Element step = _doc.createElement(STEP_TAG_NAME);

            step.setAttribute("name", _name);
            if (_status != null && _status != "") {
                step.setAttribute("status", _status);
            }

            String duration = _duration != null ? _duration.toString() : "0";
            step.setAttribute("duration", duration);

            return step;
        }
    }


}
