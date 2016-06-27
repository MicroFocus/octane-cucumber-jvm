import cucumber.api.CucumberOptions;
import org.junit.runner.RunWith;

@RunWith(OctaneCucumber.class)
@CucumberOptions(plugin = {"junit:cuc_report.xml"}, features = "src\\main\\resources")
public class RunCucumberFeatureRootFolder {

}