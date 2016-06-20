import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runners.model.InitializationError;

import java.io.IOException;

public class OctaneCucumber extends Cucumber {
    private static String[] features = null;

    private static final ThreadLocal<String[]> instance = new ThreadLocal<String[]>() {
        @Override
        protected String[] initialValue() {
            return features;
        }
    };

    public static String[] getFeatures() {
        return instance.get();
    }

    public OctaneCucumber(Class clazz) throws IOException, InitializationError {
        super(clazz);
        CucumberOptions cucumberOptions = (CucumberOptions) clazz.getAnnotation(CucumberOptions.class);
        features = cucumberOptions.features();
    }
}
