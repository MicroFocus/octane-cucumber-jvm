package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.Constants;
import com.hpe.alm.octane.infra.OutputFile;
import com.hpe.alm.octane.infra.ScenarioElement;
import cucumber.api.Argument;
import cucumber.api.PickleStepTestStep;
import cucumber.api.TestCase;
import cucumber.api.Result;
import cucumber.api.TestStep;
import cucumber.api.event.TestCaseFinished;
import cucumber.api.event.TestSourceRead;
import cucumber.api.event.TestStepFinished;
import cucumber.runtime.CucumberException;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.Resource;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.model.CucumberFeature;
import cucumber.util.Encoding;
import gherkin.AstBuilder;
import gherkin.Parser;
import gherkin.ParserException;
import gherkin.TokenMatcher;
import gherkin.ast.GherkinDocument;
import gherkin.ast.Location;
import gherkin.ast.Scenario;
import gherkin.ast.Step;
import gherkin.pickles.PickleStep;
import gherkin.pickles.PickleTag;
import org.junit.Assert;
import org.junit.Test;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OctaneGherkinFormatterTest {

    private final static String passed = Result.Type.PASSED.name();
    private final static String failed = Result.Type.FAILED.name();
    private final static String featurePath = "src\\test\\resources\\com\\hpe\\alm\\octane\\F1\\test1.feature";

    @Test
    public void testFullFlow() throws IllegalAccessException, ClassNotFoundException, InstantiationException, IOException {
        ResourceLoader resourceLoader = new MultiLoader(this.getClass().getClassLoader());
        Iterable<Resource> resources = resourceLoader.resources(featurePath, ".feature");
        ArrayList<CucumberFeature> features = new ArrayList<>();
        String source = getFeatures(resources, features);
        OctaneGherkinFormatter formatter = new OctaneGherkinFormatter(resourceLoader, features, new OutputFile(this.getClass()));
        formatter.readFeatureFile(new TestSourceRead(System.currentTimeMillis(), featurePath, source));

        String featureName = "test Feature";
        String errorMsg = "This is an error";
        int line = 2;

        ArrayList<GherkinStep> backgroundSteps = new ArrayList<>();
        backgroundSteps.add(new GherkinStep("Given","back",line++, 0L, passed));
        backgroundSteps.add(new GherkinStep("And","back",line++, 0L, passed));

        Scenario scenario = new Scenario(new ArrayList<>(), new Location(0, line++), ScenarioElement.ScenarioType.SCENARIO.getScenarioType(),"test scenario","",new ArrayList<>());
        TestCaseMock testCaseMock = new TestCaseMock(scenario, featurePath);
        addBackgroundSteps(formatter, backgroundSteps, testCaseMock);
        long step1Duration = 100;
        long step2Duration = 200;
        long step3Duration = 300;
        ArrayList<GherkinStep> scenarioSteps = new ArrayList<>();
        scenarioSteps.add(new GherkinStep("Given","test", line++, step1Duration, passed));
        scenarioSteps.add(new GherkinStep("When","test", line++, step2Duration, passed));
        scenarioSteps.add(new GherkinStep("Then","test", line++, step3Duration, passed));
        addAndRunSteps(formatter, scenarioSteps, testCaseMock);
        formatter.scenarioFinished(new TestCaseFinished(System.currentTimeMillis(),testCaseMock,
                new Result(Result.Type.PASSED, step1Duration + step2Duration + step3Duration, null)));

        ArrayList<GherkinStep> scenarioOutlineSteps = new ArrayList<>();
        scenarioOutlineSteps.add(new GherkinStep("Given","hello \"<name>\"",line++, 0L,""));
        scenarioOutlineSteps.add(new GherkinStep("When","When what \"<question>\"",line++, 0L,""));
        scenarioOutlineSteps.add(new GherkinStep("Then","Then wow",line++,(long)0,""));
        addSteps(scenarioOutlineSteps);

        Scenario scenarioOutline_1 = new Scenario(new ArrayList<>(), new Location(0, line++),
                ScenarioElement.ScenarioType.OUTLINE.getScenarioType(),
                "Table TTT","",new ArrayList<>());
        TestCaseMock testCaseMockOutline = new TestCaseMock(scenarioOutline_1, featurePath);
        addBackgroundSteps(formatter, backgroundSteps, testCaseMockOutline);

        long step7Duration = 400;
        long step8Duration = 500;
        long step9Duration = 600;
        ArrayList<GherkinStep> scenarioOutlineSteps_1 = new ArrayList<>();
        scenarioOutlineSteps_1.add(new GherkinStep("Given","hello \"Dan\"", line++,step7Duration, passed));
        scenarioOutlineSteps_1.add(new GherkinStep("When","what \"What\"", line++,step8Duration, passed));
        scenarioOutlineSteps_1.add(new GherkinStep("Then","wow", line++, step9Duration, passed, null));
        addAndRunSteps(formatter, scenarioOutlineSteps_1, testCaseMock);
        formatter.scenarioFinished(new TestCaseFinished(System.currentTimeMillis(),testCaseMockOutline,
                new Result(Result.Type.PASSED, step7Duration + step8Duration + step9Duration, null)));

        Scenario scenarioOutline_2 = new Scenario(new ArrayList<>(), new Location(0, line++),
                ScenarioElement.ScenarioType.OUTLINE.getScenarioType(),
                "Table TTT2","",new ArrayList<>());
        TestCaseMock testCaseMockOutline2 = new TestCaseMock(scenarioOutline_2, featurePath);
        addBackgroundSteps(formatter, backgroundSteps, testCaseMockOutline2);
        long step10Duration = 700;
        long step11Duration = 800;
        long step12Duration = 900;
        ArrayList<GherkinStep> scenarioOutlineSteps_2 = new ArrayList<>();
        scenarioOutlineSteps_2.add(new GherkinStep("Given","hello \"Sari\"", line++, step10Duration, passed));
        scenarioOutlineSteps_2.add(new GherkinStep("When","what \"Why\"", line++, step11Duration, passed));
        scenarioOutlineSteps_2.add(new GherkinStep("Then","wow", line, step12Duration, failed, new Exception(errorMsg)));
        addAndRunSteps(formatter, scenarioOutlineSteps_2, testCaseMock);
        formatter.scenarioFinished(new TestCaseFinished(System.currentTimeMillis(), testCaseMockOutline2,
                new Result(Result.Type.FAILED, step10Duration + step11Duration + step12Duration, null)));

        formatter.finishTestReport();
        String actualXml = formatter.getXML();
        String xmlVersion = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n";
        String expectedMessage = "<![CDATA[java.lang.Exception: " + errorMsg;
        String expectedXml = xmlVersion +
                "<features version=\"" + Constants.XML_VERSION + "\">" +
                "<feature name=\"" + featureName + "\" " +
                "path=\"" + featurePath + "\"" +
                " started=\"\"" +
                " tag=\"@TID2001REV0.2.0\">" +
                "<file><![CDATA[" + source + "]]></file>" +
                "<scenarios>" +
                "<background>" +
                "<steps>" +
                "<step duration=\"0\" name=\"Given back\" status=\"passed\"/>" +
                "<step duration=\"0\" name=\"And back\" status=\"passed\"/>" +
                "</steps>" +
                "</background>"+
                "<scenario name=\"test scenario\">" +
                "<steps>" +
                "<step duration=\"" + step1Duration + "\" name=\"Given test\" status=\"passed\"/>" +
                "<step duration=\"" + step2Duration + "\" name=\"When test\" status=\"passed\"/>" +
                "<step duration=\"" + step3Duration + "\" name=\"Then test\" status=\"passed\"/>" +
                "</steps>" +
                "</scenario>" +
                "<scenario name=\"Table TTT\" outlineIndex=\"1\">" +
                "<steps>" +
                "<step duration=\"" + step7Duration + "\" name=\"Given hello &quot;Dan&quot;\" status=\"passed\"/>" +
                "<step duration=\"" + step8Duration + "\" name=\"When what &quot;What&quot;\" status=\"passed\"/>" +
                "<step duration=\"" + step9Duration + "\" name=\"Then wow\" status=\"passed\"/>" +
                "</steps>" +
                "</scenario>" +
                "<scenario name=\"Table TTT\" outlineIndex=\"2\">" +
                "<steps>" +
                "<step duration=\"" + step10Duration + "\" name=\"Given hello &quot;Sari&quot;\" status=\"passed\"/>" +
                "<step duration=\"" + step11Duration + "\" name=\"When what &quot;Why&quot;\" status=\"passed\"/>" +
                "<step duration=\"" + step12Duration + "\" name=\"Then wow\" status=\"failed\">" +
                "<error_message>" + expectedMessage + "></error_message></step>" +
                "</steps>" +
                "</scenario>" +
                "</scenarios>" +
                "</feature>" +
                "</features>";

        actualXml = removeStartedFromXml(actualXml);
        actualXml = removeStackTraceFromXML(actualXml, expectedMessage);
        Assert.assertEquals("Differences were found between actual report xml and expected xml: ", expectedXml, actualXml);
    }

    private String getFeatures(Iterable<Resource> resources, ArrayList<CucumberFeature> features) throws IOException {
        CucumberFeature cucumberFeature;
        String source = null;
        try {

            Parser<GherkinDocument> parser = new Parser<>(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();
            for (Resource resource : resources) {
                source = Encoding.readFile(resource);
                GherkinDocument gherkinDocument = parser.parse(source, matcher);
                cucumberFeature = new CucumberFeature(gherkinDocument, featurePath, source);
                features.add(cucumberFeature);
            }

        }
        catch (ParserException e) {
            throw new CucumberException(e);
        }
        return source;
    }

    private void addBackgroundSteps(OctaneGherkinFormatter formatter, ArrayList<GherkinStep> gherkinSteps, TestCaseMock testCaseMock) {
        addAndRunSteps(formatter, gherkinSteps, testCaseMock);
    }


    private ArrayList<Step> addSteps(ArrayList<GherkinStep> gherkinSteps) {
        ArrayList<Step> steps = new ArrayList<>();
        for(GherkinStep gherkinStep : gherkinSteps){
            Step step = new Step(new Location(0, gherkinStep.getLine()),gherkinStep.getKeyword(),gherkinStep.getName(),null);
            steps.add(step);
        }
        return steps;
    }

    private void runSteps(OctaneGherkinFormatter formatter, ArrayList<GherkinStep> gherkinSteps, ArrayList<Step> runTimeSteps, TestCaseMock testCase){
        int gherkinStepIndex = 0;
        for (Step runTimeStep : runTimeSteps) {
            PickleStepTestStepMock pickleStepTestStepMock = new PickleStepTestStepMock (runTimeStep) ;
            Result.Type status = Result.Type.fromLowerCaseName(gherkinSteps.get(gherkinStepIndex).getStatus());
            Throwable exception = null;
            if(status.equals(Result.Type.FAILED)) {
                exception = gherkinSteps.get(gherkinStepIndex).getError();
            }
            Result result = new Result(status, gherkinSteps.get(gherkinStepIndex).getDuration(), exception);
            TestStepFinished testStepFinished = new TestStepFinished(System.currentTimeMillis(), testCase, pickleStepTestStepMock, result);
            formatter.testStepFinished(testStepFinished);
            gherkinStepIndex++;
        }
    }

    private void addAndRunSteps(OctaneGherkinFormatter formatter, ArrayList<GherkinStep> gherkinSteps, TestCaseMock testCaseMock){
        ArrayList<Step> runTimeSteps = addSteps(gherkinSteps);
        runSteps(formatter, gherkinSteps, runTimeSteps, testCaseMock);
    }

    private String removeStartedFromXml(String xml){
        String startedString = " started=\"";
        int index1 = xml.indexOf(startedString);
        int index2 = xml.indexOf("\"",index1 + startedString.length());
        String section1 = xml.substring(0, index1 + startedString.length());
        String section2 = xml.substring(index2);
        return section1 + section2;
    }

    private String removeStackTraceFromXML(String xml, String errorMessage){
        int index1 = xml.indexOf(errorMessage);
        int index2 = xml.indexOf(">",index1 + errorMessage.length());
        String section1 = xml.substring(0, index1 + errorMessage.length());
        String section2 = xml.substring(index2);
        return section1 + section2;
    }

    private class TestCaseMock implements TestCase {

        private Scenario scenario;
        private String uri;
        TestCaseMock(Scenario scenario, String uri){
            this.scenario = scenario;
            this.uri = uri;
        }

        @Override
        public int getLine() {
            return scenario.getLocation().getLine();
        }

        @Override
        public String getName() {
            return scenario.getName();
        }

        @Override
        public String getScenarioDesignation() {
            return null;
        }

        @Override
        public List<PickleTag> getTags() {
            return null;
        }

        @Override
        public List<TestStep> getTestSteps() {
            return null;
        }

        @Override
        public String getUri() {
            return uri;
        }
    }

    private class PickleStepTestStepMock implements PickleStepTestStep {

        private Step step;
        PickleStepTestStepMock(Step step) {
            this.step = step;
        }

        @Override
        public String getPattern() {
            return null;
        }

        @Override
        public PickleStep getPickleStep() {
            return null;
        }

        @Override
        public List<Argument> getDefinitionArgument() {
            return null;
        }

        @Override
        public List<gherkin.pickles.Argument> getStepArgument() {
            return null;
        }

        @Override
        public int getStepLine() {
            return 0;
        }

        @Override
        public String getStepLocation() {
            return null;
        }

        @Override
        public String getStepText() {
            return step.getText();
        }

        @Override
        public String getCodeLocation() {
            return null;
        }
    }

    private class GherkinStep {
        String keyword;
        String name;
        Integer line;
        Long duration;
        String status;
        Throwable error;

        GherkinStep(String keyword, String name, Integer line, Long duration, String status) {
            this.keyword = keyword;
            this.name = name;
            this.line = line;
            this.duration = duration;
            this.status = status;
        }

        GherkinStep(String keyword, String name, Integer line, Long duration, String status, Throwable error) {
            this(keyword, name, line, duration, status);
            this.error = error;
        }

        private String getKeyword() {
            return keyword;
        }

        private String getName() {
            return name;
        }

        private Integer getLine() {
            return line;
        }

        private Long getDuration() {
            return duration;
        }

        private String getStatus() {
            return status;
        }

        private Throwable getError() {
            return error;
        }
    }
}
