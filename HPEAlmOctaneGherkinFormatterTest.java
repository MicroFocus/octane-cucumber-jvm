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
        String featurePath = "src\\main\\features";
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

        formatter.background(null);
        line++;

        Step backgroundStep1 = new Step(null,"Given","back",line++,null,null);
        formatter.step(backgroundStep1);

        Step backgroundStep2 = new Step(null,"And","back",line++,null,null);
        formatter.step(backgroundStep2);

        long backgroundStep1Duration = (long)400;
        formatter.match(new StepDefinitionMatch(null,getStepDefinition(),null,backgroundStep1,null));
        formatter.result(new Result(passed,backgroundStep1Duration,null,null));

        long backgroundStep2Duration = (long)500;
        formatter.match(new StepDefinitionMatch(null,getStepDefinition(),null,backgroundStep1,null));
        formatter.result(new Result(passed,backgroundStep1Duration,null,null));

        Scenario scenario = new Scenario(null,null,"","test scenario","",line++,"2000");
        formatter.scenario(scenario);

        Step step1 = new Step(null,"Given","test",line++,null,null);
        formatter.step(step1);

        Step step2 = new Step(null,"When","test",line++,null,null);
        formatter.step(step2);

        Step step3 = new Step(null,"Then","test",line++,null,null);
        formatter.step(step3);

        long step1Duration = (long)100;
        formatter.match(new StepDefinitionMatch(null,getStepDefinition(),null,step1,null));
        formatter.result(new Result(passed,step1Duration,null,null));

        long step2Duration = (long)200;
        formatter.match(new StepDefinitionMatch(null,getStepDefinition(),null,step2,null));
        formatter.result(new Result(passed,step2Duration,null,null));

        long step3Duration = (long)300;
        formatter.match(new StepDefinitionMatch(null,getStepDefinition(),null,step3,null));
        formatter.result(new Result(failed,step3Duration,null,null));

        formatter.endOfScenarioLifeCycle(scenario);
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
}
