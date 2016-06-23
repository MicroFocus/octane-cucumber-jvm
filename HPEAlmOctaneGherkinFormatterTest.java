import gherkin.formatter.model.Step;
import org.easymock.EasyMock;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.ArrayList;

public class HPEAlmOctaneGherkinFormatterTest {
    private Document doc = null;
    private HPEAlmOctaneGherkinFormatter formatter;
    private String passed = "passed";
    private String skipped = "skipped";
    private String failed = "failed";
    private Long started = Long.parseLong("1464856989165");
    private Long stepDuration = Long.parseLong("1111111");
    private String path = "src\\test\\java\\G2_10033.feature";
    private String featureName = "Some terse yet descriptive text of what is desired like bla bla";

    private String backgroundStep1 = "Given a global administrator named \"Greg\"";
    private String backgroundStep2 = "And a blog named \"Greg's anti-tax rants\"";
    private String backgroundStep3 = "And a customer named \"Wilson\"";

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

    private String scenarioOutline = "feeding a cow";
    private String scenarioOutlineStep1 = "Given the cow weighs %s kg";
    private String scenarioOutlineStep2 = "When we calculate the feeding requirements";
    private String scenarioOutlineStep3 = "Then the energy should be %s MJ";
    private String scenarioOutlineWeight1 = "125";
    private String scenarioOutlineWeight2 = "135";
    private String scenarioOutlineEnergy1 = "26500";
    private String scenarioOutlineEnergy2 = "29500";

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
            scenario2Step4 + "\n" +
            "               Scenario Outline: " + scenarioOutline + "\n" +
            String.format(scenarioOutlineStep1, "<weight>") + "\n" +
            scenarioOutlineStep2 + "\n" +
            String.format(scenarioOutlineStep3, "<energy>") + "\n" +
            "\n" +
            "               Examples:\n" +
            "                   | weight | energy |\n" +
            "                   |  "+scenarioOutlineWeight1+"   |  "+scenarioOutlineEnergy1+" |\n" +
            "                   |  "+scenarioOutlineWeight2+"   |  "+scenarioOutlineEnergy2+" |\n";

    private String xmlVersion = "<?xml version=\"1.0\" encoding=\"UTF-16\"?>\n";
    private String emptyFeature = "<feature name=\"\" path=\"\"><file><![CDATA[]]></file>%s</feature>";
    private String emptyScenario = "<scenario name=\"\">%s</scenario>";
    private String emptyStep = "<step duration=\"0\" name=\"\"/>";

    private String gherkinNGAResultsXml = xmlVersion +
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
            "<scenario name=\""+scenarioOutline+"\" outlineIndex=\"1\">" +
            "<steps>" +
            "<step duration=\""+(stepDuration+11)+"\" name=\""+ String.format(scenarioOutlineStep1, scenarioOutlineWeight1)+"\" status=\"passed\"/>" +
            "<step duration=\""+(stepDuration+12)+"\" name=\""+scenarioOutlineStep2+"\" status=\"passed\"/>" +
            "<step duration=\""+(stepDuration+13)+"\" name=\""+ String.format(scenarioOutlineStep3, scenarioOutlineEnergy1)+"\" status=\"passed\"/>" +
            "</steps>" +
            "</scenario>" +
            "<scenario name=\""+scenarioOutline+"\" outlineIndex=\"2\">" +
            "<steps>" +
            "<step duration=\""+(stepDuration+14)+"\" name=\""+ String.format(scenarioOutlineStep1, scenarioOutlineWeight2)+"\" status=\"passed\"/>" +
            "<step duration=\""+(stepDuration+15)+"\" name=\""+scenarioOutlineStep2+"\" status=\"passed\"/>" +
            "<step duration=\""+(stepDuration+16)+"\" name=\""+ String.format(scenarioOutlineStep3, scenarioOutlineEnergy2)+"\" status=\"passed\"/>" +
            "</steps>" +
            "</scenario>" +
            "</scenarios>" +
            "</feature>";

    @Before
    public void init() throws ParserConfigurationException {
        formatter = new HPEAlmOctaneGherkinFormatter(null,new ArrayList<String>());
        doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    @Test
    public void testFeatureElementToXMLElement() {
        ScenarioElement scenarioElement1 = new ScenarioElement(scenarioName1);
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step1,1,passed,stepDuration+1));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step2,2,passed,stepDuration+2));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step3,3,passed,stepDuration+3));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step4,4,passed,stepDuration+4));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step5,5,passed,stepDuration+5));
        scenarioElement1.getSteps().add(createStepElement("",scenario1Step6,6,passed,stepDuration+6));

        ScenarioElement scenarioElement2 =  new ScenarioElement(scenarioName2);
        scenarioElement2.getSteps().add(createStepElement("",scenario2Step1,1,failed,stepDuration+7));
        scenarioElement2.getSteps().add(createStepElement("",scenario2Step2,2,skipped,(long)0));
        scenarioElement2.getSteps().add(createStepElement("",scenario2Step3,3,skipped,(long)0));
        scenarioElement2.getSteps().add(createStepElement("",scenario2Step4,4,skipped,(long)0));

        ScenarioElement scenarioOutlineElement1 =  new ScenarioElement(scenarioOutline,1);
        scenarioOutlineElement1.getSteps().add(createStepElement("", String.format(scenarioOutlineStep1,scenarioOutlineWeight1),1,passed,stepDuration+11));
        scenarioOutlineElement1.getSteps().add(createStepElement("",scenarioOutlineStep2,2,passed,stepDuration+12));
        scenarioOutlineElement1.getSteps().add(createStepElement("", String.format(scenarioOutlineStep3, scenarioOutlineEnergy1),3,passed,stepDuration+13));

        ScenarioElement scenarioOutlineElement2 =  new ScenarioElement(scenarioOutline,2);
        scenarioOutlineElement2.getSteps().add(createStepElement("", String.format(scenarioOutlineStep1,scenarioOutlineWeight2),1,passed,stepDuration+14));
        scenarioOutlineElement2.getSteps().add(createStepElement("",scenarioOutlineStep2,2,passed,stepDuration+15));
        scenarioOutlineElement2.getSteps().add(createStepElement("", String.format(scenarioOutlineStep3, scenarioOutlineEnergy2),3,passed,stepDuration+16));

        FeatureElement featureElement = new FeatureElement();
        featureElement.setName(featureName);
        featureElement.setStarted(started);
        featureElement.setPath(path);
        featureElement.setFile(gherkinScript);

        featureElement.getBackgroundSteps().add(createStepElement("",backgroundStep1,1,passed,stepDuration+8));
        featureElement.getBackgroundSteps().add(createStepElement("",backgroundStep2,2,passed,stepDuration+9));
        featureElement.getBackgroundSteps().add(createStepElement("",backgroundStep3,3,passed,stepDuration+10));

        featureElement.getScenarios().add(scenarioElement1);
        featureElement.getScenarios().add(scenarioElement2);
        featureElement.getScenarios().add(scenarioOutlineElement1);
        featureElement.getScenarios().add(scenarioOutlineElement2);

        Assert.assertEquals(gherkinNGAResultsXml,elementToString(featureElement.toXMLElement(doc)));
    }

    @Test
    public void testEmptyFeatureElementInfo() {
        String emptyFeatureInfo = xmlVersion + String.format(emptyFeature, "<scenarios/>");
        FeatureElement featureElement = new FeatureElement();
        Assert.assertEquals(emptyFeatureInfo,elementToString(featureElement.toXMLElement(doc)));
    }

    @Test
    public void testEmptyScenarioElementInfo() {
        String scenarios = "<scenarios>" + String.format(emptyScenario, "<steps/>") + String.format(emptyScenario, "<steps/>") + "</scenarios>";
        String emptyFeatureAndScenarioInfo = xmlVersion + String.format(emptyFeature, scenarios);
        FeatureElement featureElement = new FeatureElement();
        ScenarioElement scenarioElement1 = new ScenarioElement(null);
        ScenarioElement scenarioElement2 = new ScenarioElement("");
        featureElement.getScenarios().add(scenarioElement1);
        featureElement.getScenarios().add(scenarioElement2);
        Assert.assertEquals(emptyFeatureAndScenarioInfo,elementToString(featureElement.toXMLElement(doc)));
    }

    @Test
    public void testEmptyStepElementInfo() {
        String steps = "<steps>" + emptyStep + emptyStep + "</steps>";
        String scenarios = "<scenarios>" + String.format(emptyScenario, steps) + "</scenarios>";
        String emptyFeatureAndScenarioInfo = xmlVersion + String.format(emptyFeature, scenarios);
        FeatureElement featureElement = new FeatureElement();
        ScenarioElement scenarioElement1 = new ScenarioElement("");
        scenarioElement1.getSteps().add(new StepElement(null));
        scenarioElement1.getSteps().add(new StepElement(createStep("", "", -1)));
        featureElement.getScenarios().add(scenarioElement1);
        Assert.assertEquals(emptyFeatureAndScenarioInfo,elementToString(featureElement.toXMLElement(doc)));
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

    private StepElement createStepElement(String keyword, String name, Integer line, String status, Long duration) {
        StepElement stepElement = new StepElement(createStep(keyword, name, line));
        stepElement.setStatus(status);
        stepElement.setDuration(duration);
        return stepElement;
    }
}
