import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(plugin = {"junit:cuc_report.xml"}, features = "src\\main\\resources\\F1\\test1.feature")
public class RunCucumberFeatureFileTest {

}