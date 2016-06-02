import gherkin.formatter.model.Step;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

public class GherkinNGAFormatterTest {
    private GherkinNGAFormatter gherkinNGAFormatter;
    private String passed = "passed";
    private String skipped = "skipped";
    private String failed = "failed";
    private Long started = Long.parseLong("1464856989165");
    private Long stepDuration = Long.parseLong("1111111");
    private String path = "src\\test\\java\\G2_10033.feature";
    private String featureName = "Some terse yet descriptive text of what is desired like bla bla";
    private String scenarioName1 = "Some determinable business situation";
    private String scenario1Step1 = "Given the following people exist: Aila and Joe";
    private String scenario1Step2 = "And some precondition 1";
    private String scenario1Step3 = "When some action by the actor";
    private String scenario1Step4 = "And some other action";
    private String scenario1Step5 = "Then some testable outcome is achieved";
    private String scenario1Step6 = "And something else we can check happens too";
    private String scenarioName2 = "Some another scenario 2";
    private String scenario2Step1 = "Given some precondition";
    private String scenario2Step2 = "When some action by the actor";
    private String scenario2Step3 = "Then some testable outcome is achieved";
    private String scenario2Step4 = "But I don't see something else";
    private String backgroundStep1 = "Given a global administrator named \"Greg\"";
    private String backgroundStep2 = "And a blog named \"Greg's anti-tax rants\"";
    private String backgroundStep3 = "And a customer named \"Wilson\"";
    private String gherkinScript = "[#Auto generated NGA revision tag\n" +
            "@TID1003REV0.4.0\n" +
            "@billing @bicker @annoy\n" +
            "Feature: " + featureName + "\n" +
            "                              Realize a named business value\n" +
            "                              As an explicit system actor\n" +
            "\n" +
            "               Background:\n" +
            "                              Given a global administrator named \"Greg\"\n" +
            "                              And a blog named \"Greg's anti-tax rants\"\n" +
            "                              And a customer named \"Wilson\"" +
            "\n" +
            "               Scenario: " + scenarioName1 + "\n" +
                                            scenario1Step2 + "\n" +
                                            scenario1Step3 + "\n" +
                                            scenario1Step4 + "\n" +
                                            scenario1Step5 + "\n" +
                                            scenario1Step6 + "\n" +
            "               Scenario: " + scenarioName2 + "\n" +
                                            "This is scenario 2 description\n" +
                                            scenario2Step1 + "\n" +
                                            scenario2Step2 + "\n" +
                                            scenario2Step3 + "\n" +
                                            scenario2Step4 + "\n";

    //<background><steps><step duration="0" name="Given a global administrator named &quot;Greg&quot;"/><step duration="0" name="And a blog named &quot;Greg's anti-tax rants&quot;"/><step duration="0" name="And a customer named &quot;Wilson&quot;"/></steps></background>

    private String gherkinNGAResultsXml = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n" +
                "<feature name=\""+featureName+"\" path=\""+path+"\" started=\""+started+"\">"+
                    "<file><![CDATA[" + gherkinScript +"]]></file>"+
                    "<scenarios>" +
                        "<background>" +
                            "<steps>" +
                                "<step duration=\""+(stepDuration+8)+"\" name=\""+backgroundStep1.replace("\"","&quot;")+"\" status=\"passed\"/>" +
                                "<step duration=\""+(stepDuration+9)+"\" name=\""+backgroundStep2.replace("\"","&quot;")+"\" status=\"passed\"/>" +
                                "<step duration=\""+(stepDuration+10)+"\" name=\""+backgroundStep3.replace("\"","&quot;")+"\" status=\"passed\"/>" +
                            "</steps>" +
                        "</background>" +
                        "<scenario name=\""+scenarioName1+"\">" +
                            "<steps>" +
                                "<step duration=\""+(stepDuration+1)+"\" name=\""+scenario1Step1+"\" status=\"passed\"/>" +
                                "<step duration=\""+(stepDuration+2)+"\" name=\""+scenario1Step2+"\" status=\"passed\"/>" +
                                "<step duration=\""+(stepDuration+3)+"\" name=\""+scenario1Step3+"\" status=\"passed\"/>" +
                                "<step duration=\""+(stepDuration+4)+"\" name=\""+scenario1Step4+"\" status=\"passed\"/>" +
                                "<step duration=\""+(stepDuration+5)+"\" name=\""+scenario1Step5+"\" status=\"passed\"/>" +
                                "<step duration=\""+(stepDuration+6)+"\" name=\""+scenario1Step6+"\" status=\"passed\"/>" +
                            "</steps>" +
                        "</scenario>" +
                        "<scenario name=\""+scenarioName2+"\">" +
                            "<steps>" +
                                "<step duration=\""+(stepDuration+7)+"\" name=\""+scenario2Step1+"\" status=\"failed\"/>" +
                                "<step duration=\"0\" name=\""+scenario2Step2+"\" status=\"skipped\"/>" +
                                "<step duration=\"0\" name=\""+scenario2Step3+"\" status=\"skipped\"/>" +
                                "<step duration=\"0\" name=\""+scenario2Step4+"\" status=\"skipped\"/>" +
                            "</steps>" +
                        "</scenario>" +
                    "</scenarios>" +
                "</feature>";

    @Before
    public void init() {
        gherkinNGAFormatter = new GherkinNGAFormatter();
    }

    @Test
    public void testFeatureElementToXMLElement() {
        GherkinNGAFormatter.ScenarioElement scenarioElement1 = gherkinNGAFormatter.new ScenarioElement(scenarioName1);
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step1,1,passed,stepDuration+1));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step2,2,passed,stepDuration+2));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step3,3,passed,stepDuration+3));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step4,4,passed,stepDuration+4));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step5,5,passed,stepDuration+5));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step6,6,passed,stepDuration+6));

        GherkinNGAFormatter.ScenarioElement scenarioElement2 =  gherkinNGAFormatter.new ScenarioElement(scenarioName2);
        scenarioElement2.getSteps().add(createStepElement("",scenario2Step1,1,failed,stepDuration+7));
        scenarioElement2.getSteps().add(createStepElement("",scenario2Step2,2,skipped,(long)0));
        scenarioElement2.getSteps().add(createStepElement("",scenario2Step3,3,skipped,(long)0));
        scenarioElement2.getSteps().add(createStepElement("",scenario2Step4,4,skipped,(long)0));

        GherkinNGAFormatter.FeatureElement featureElement = gherkinNGAFormatter.new FeatureElement();
        featureElement.setName(featureName);
        featureElement.setStarted(started);
        featureElement.setPath(path);
        featureElement.setFile(gherkinScript);

        featureElement.getBackgroundSteps().add(createStepElement("",backgroundStep1,1,passed,stepDuration+8));
        featureElement.getBackgroundSteps().add(createStepElement("",backgroundStep2,2,passed,stepDuration+9));
        featureElement.getBackgroundSteps().add(createStepElement("",backgroundStep3,3,passed,stepDuration+10));

        featureElement.getScenarios().add(scenarioElement1);
        featureElement.getScenarios().add(scenarioElement2);

        Assert.assertEquals(gherkinNGAResultsXml,elementToString(featureElement.toXMLElement()));
    }

    private String elementToString(Element element) {
        Document document = element.getOwnerDocument();
        DOMImplementationLS domImplLS = (DOMImplementationLS) document
                .getImplementation();
        LSSerializer serializer = domImplLS.createLSSerializer();
        return serializer.writeToString(element);
    }

    private Step createStep(String keyword, String name, Integer line) {
        Step step = EasyMock.createMock(Step.class);
        EasyMock.expect(step.getKeyword()).andReturn(keyword).anyTimes();
        EasyMock.expect(step.getName()).andReturn(name).anyTimes();
        EasyMock.expect(step.getLine()).andReturn(line).anyTimes();
        EasyMock.replay(step);
        return step;
    }

    private GherkinNGAFormatter.StepElement createStepElement(String keyword, String name, Integer line, String status, Long duration) {
        GherkinNGAFormatter.StepElement stepElement = gherkinNGAFormatter.new StepElement(createStep(keyword, name, line));
        stepElement.setStatus(status);
        stepElement.setDuration(duration);
        return stepElement;
    }
}
