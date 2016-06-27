import cucumber.runtime.StepDefinition;
import cucumber.runtime.StepDefinitionMatch;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import gherkin.formatter.model.*;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class HPEAlmOctaneGherkinFormatterTest {

    private String xmlVersion = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n";
    private String passed = "Passed";
    private String failed = "Failed";

    @Test
    public void testNoExceptionsAreThrownForFormatterInterface() {
        HPEAlmOctaneGherkinFormatter formatter = new HPEAlmOctaneGherkinFormatter(null, new ArrayList<String>());
        formatter.syntaxError(null,null,null,null,0);
        formatter.uri(null);
        formatter.feature(null);
        formatter.scenarioOutline(null);
        formatter.examples(null);
        formatter.startOfScenarioLifeCycle(null);
        formatter.background(null);
        formatter.scenario(null);
        formatter.step(null);
        formatter.endOfScenarioLifeCycle(null);
        formatter.done();
        formatter.close();
        formatter.eof();
    }

    @Test
    public void testNoExceptionsAreThrownForFormatterReporterInterface() {
        HPEAlmOctaneGherkinFormatter formatter = new HPEAlmOctaneGherkinFormatter(null, new ArrayList<String>());
        formatter.before(null,null);
        formatter.result(null);
        formatter.after(null,null);
        formatter.match(null);
        formatter.embedding(null,null);
        formatter.write(null);
    }

    @Test
    public void testFullFlow() throws IllegalAccessException, ClassNotFoundException, InstantiationException, IOException {
        runFullFlow(false);
    }

    @Test
    public void testFullFlowWithExceptions() throws IllegalAccessException, InstantiationException, ClassNotFoundException {
        runFullFlow(true);
    }

    private void runFullFlow(boolean withExceptions) throws IllegalAccessException, ClassNotFoundException, InstantiationException {
        String testTag = "@TID1003REV0.4.0";
        String featurePath = "src\\main\\resources";
        String featureFileName = "test1.feature";

        ArrayList<String> features = new ArrayList<>();
        features.add(featurePath);

        ClassLoader classLoader = this.getClass().getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        HPEAlmOctaneGherkinFormatter formatter = new HPEAlmOctaneGherkinFormatter(resourceLoader, features);
        formatter.setThrowException(withExceptions);

        formatter.uri(featureFileName);

        String featureName = "test Feature";
        int line = 2;
        Tag tag = new Tag(testTag,line++);
        ArrayList<Tag> tags = new ArrayList<>();
        tags.add(tag);
        formatter.feature(new Feature(null,tags,"",featureName,"",line++,"1000"));

        ArrayList<GherkinStep> backgroundSteps = new ArrayList<>();
        backgroundSteps.add(new GherkinStep("Given","back",line++,(long)0,passed));
        backgroundSteps.add(new GherkinStep("And","back",line++,(long)0,passed));
        addBackgroundSteps(formatter, backgroundSteps);

        Scenario scenario = new Scenario(null,null,"","test scenario","",line++,"2000");
        formatter.scenario(scenario);
        long step1Duration = (long)100;
        long step2Duration = (long)200;
        long step3Duration = (long)300;
        ArrayList<GherkinStep> scenario1Steps = new ArrayList<>();
        scenario1Steps.add(new GherkinStep("Given","test",line++,step1Duration,passed));
        scenario1Steps.add(new GherkinStep("When","test",line++,step2Duration,passed));
        scenario1Steps.add(new GherkinStep("Then","test",line++,step3Duration,failed));
        addAndRunSteps(formatter, scenario1Steps);
        formatter.endOfScenarioLifeCycle(scenario);

        formatter.scenarioOutline(null);
        ArrayList<GherkinStep> scenarioOutlineSteps = new ArrayList<>();
        scenario1Steps.add(new GherkinStep("Given","hello \"<name>\"",line++,(long)0,""));
        scenario1Steps.add(new GherkinStep("When","When what \"<question>\"",line++,(long)0,""));
        scenario1Steps.add(new GherkinStep("Then","Then wow",line++,(long)0,""));
        addSteps(formatter, scenarioOutlineSteps);

        addBackgroundSteps(formatter, backgroundSteps);

        Scenario scenarioOutline_1 = new Scenario(null,null,"Scenario Outline","Table TTT","",line++,"3000");
        formatter.scenario(scenarioOutline_1);
        long step7Duration = (long)400;
        long step8Duration = (long)500;
        long step9Duration = (long)600;
        ArrayList<GherkinStep> scenarioOutlineSteps_1 = new ArrayList<>();
        scenarioOutlineSteps_1.add(new GherkinStep("Given","hello \"Dan\"",line++,step7Duration,passed));
        scenarioOutlineSteps_1.add(new GherkinStep("When","When what \"What\"",line++,step8Duration,passed));
        scenarioOutlineSteps_1.add(new GherkinStep("Then","Then wow",line++,step9Duration,failed));
        addAndRunSteps(formatter, scenarioOutlineSteps_1);
        formatter.endOfScenarioLifeCycle(scenarioOutline_1);

        Scenario scenarioOutline_2 = new Scenario(null,null,"Scenario Outline","Table TTT","",line++,"3000");
        formatter.scenario(scenarioOutline_2);
        long step10Duration = (long)700;
        long step11Duration = (long)800;
        long step12Duration = (long)900;
        ArrayList<GherkinStep> scenarioOutlineSteps_2 = new ArrayList<>();
        scenarioOutlineSteps_2.add(new GherkinStep("Given","hello \"Sari\"",line++,step10Duration,passed));
        scenarioOutlineSteps_2.add(new GherkinStep("When","When what \"Why\"",line++,step11Duration,passed));
        scenarioOutlineSteps_2.add(new GherkinStep("Then","Then wow",line++,step12Duration,failed));
        addAndRunSteps(formatter, scenarioOutlineSteps_2);
        formatter.endOfScenarioLifeCycle(scenarioOutline_2);

        formatter.eof();
        String actualXml = formatter.getXML();
        formatter.done();
        formatter.close();

        String expectedXml = "";
        if(!withExceptions){
            expectedXml = xmlVersion +
                    "<features>" +
                        "<feature name=\"" + featureName +"\" " +
                                "path=\""+featurePath+ "\\" + featureFileName + "\"" +
                                " started=\"\"" +
                                " tag=\""+testTag+"\">" +
                            "<file><![CDATA["+getScript(featureFileName)+"]]></file>" +
                            "<scenarios>" +
                                "<background>" +
                                    "<steps>" +
                                        "<step duration=\"0\" name=\"Givenback\"/>" +
                                        "<step duration=\"0\" name=\"Andback\"/>" +
                                    "</steps>" +
                                "</background>"+
                                "<scenario name=\"test scenario\">" +
                                    "<steps>" +
                                        "<step duration=\""+step1Duration+"\" name=\"Giventest\" status=\"Passed\"/>" +
                                        "<step duration=\""+step2Duration+"\" name=\"Whentest\" status=\"Passed\"/>" +
                                        "<step duration=\""+step3Duration+"\" name=\"Thentest\" status=\"Failed\"/>" +
                                    "</steps>" +
                                "</scenario>" +
                                    "<scenario name=\"Table TTT\" outlineIndex=\"1\">" +
                                        "<steps>" +
                                            "<step duration=\""+step7Duration+"\" name=\"Givenhello &quot;Dan&quot;\" status=\"Passed\"/>" +
                                            "<step duration=\""+step8Duration+"\" name=\"WhenWhen what &quot;What&quot;\" status=\"Passed\"/>" +
                                            "<step duration=\""+step9Duration+"\" name=\"ThenThen wow\" status=\"Failed\"/>" +
                                        "</steps>" +
                                    "</scenario>" +
                                    "<scenario name=\"Table TTT\" outlineIndex=\"2\">" +
                                        "<steps>" +
                                            "<step duration=\""+step10Duration+"\" name=\"Givenhello &quot;Sari&quot;\" status=\"Passed\"/>" +
                                            "<step duration=\""+step11Duration+"\" name=\"WhenWhen what &quot;Why&quot;\" status=\"Passed\"/>" +
                                            "<step duration=\""+step12Duration+"\" name=\"ThenThen wow\" status=\"Failed\"/>" +
                                        "</steps>" +
                                    "</scenario>" +
                            "</scenarios>" +
                        "</feature>" +
                    "</features>";

            actualXml = removeStartedFromXml(actualXml);
        } else {
            expectedXml = xmlVersion +
                    "<features><feature name=\"\" path=\"\" tag=\"\"><file><![CDATA[]]></file><scenarios/></feature></features>";
        }

        Assert.assertEquals(expectedXml,actualXml);
    }

    private void addBackgroundSteps(HPEAlmOctaneGherkinFormatter formatter, ArrayList<GherkinStep> gherkinSteps) {
        formatter.background(null);
        addAndRunSteps(formatter,gherkinSteps);
    }


    private ArrayList<Step> addSteps(HPEAlmOctaneGherkinFormatter formatter, ArrayList<GherkinStep> gherkinSteps) {
        ArrayList<Step> steps = new ArrayList<>();
        for(GherkinStep gherkinStep : gherkinSteps){
            Step step = new Step(null,gherkinStep.getKeyword(),gherkinStep.getName(),gherkinStep.getLine(),null,null);
            formatter.step(step);
            steps.add(step);
        }
        return steps;
    }

    private void runSteps(HPEAlmOctaneGherkinFormatter formatter, ArrayList<GherkinStep> gherkinSteps,ArrayList<Step> runTimeSteps ){
        for(int i=0 ; i < runTimeSteps.size() ; i++){
            formatter.match(new StepDefinitionMatch(null,getStepDefinition(),null,runTimeSteps.get(i),null));
            formatter.result(new Result(gherkinSteps.get(i).getStatus(),gherkinSteps.get(i).getDuration(),null,null));
        }
    }

    private void addAndRunSteps(HPEAlmOctaneGherkinFormatter formatter, ArrayList<GherkinStep> gherkinSteps){
        ArrayList<Step> runTimeSteps = addSteps(formatter,gherkinSteps);
        runSteps(formatter,gherkinSteps,runTimeSteps);
    }

    private StepDefinition getStepDefinition() {
        StepDefinition stepDefinition = EasyMock.createMock(StepDefinition.class);
        EasyMock.expect(stepDefinition.getLocation(false)).andReturn("").anyTimes();
        EasyMock.replay(stepDefinition);
        return stepDefinition;
    }

    private String removeStartedFromXml(String xml){
        String str = " started=\"";
        int index1 = xml.indexOf(" started=\"");
        int index2 = xml.indexOf("\"",index1+str.length());
        String section1 = xml.substring(0,index1+str.length());
        String section2 = xml.substring(index2);
        return section1 + section2;
    }

    private String getScript(String file){
        InputStream inputStream = getClass().getResourceAsStream(file);
        String result = new BufferedReader(new InputStreamReader(inputStream)).lines().collect(Collectors.joining("\n"));
        return result.replace("\n","\r\n");
    }

    private class GherkinStep {
        String keyword;
        String name;
        Integer line;
        Long duration;
        String status;

        public GherkinStep(String keyword, String name, Integer line, Long duration, String status) {
            this.keyword = keyword;
            this.name = name;
            this.line = line;
            this.duration = duration;
            this.status = status;
        }

        public String getKeyword() {
            return keyword;
        }

        public String getName() {
            return name;
        }

        public Integer getLine() {
            return line;
        }

        public Long getDuration() {
            return duration;
        }

        public String getStatus() {
            return status;
        }
    }
}
