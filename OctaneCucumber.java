import cucumber.api.CucumberOptions;
import cucumber.api.junit.Cucumber;
import org.junit.runners.model.InitializationError;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.*;

public class OctaneCucumber extends Cucumber {
    private String OCTANE_FORMATTER = "HPEAlmOctaneGherkinFormatter";
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
        addFormatterToOptions(cucumberOptions);
    }

    private void addFormatterToOptions(CucumberOptions cucumberOptions) {
        try {
            Object handler = Proxy.getInvocationHandler(cucumberOptions);
            Field memberValuesField = handler.getClass().getDeclaredField("memberValues");
            memberValuesField.setAccessible(true);
            Map<String, Object> memberValues = (Map<String, Object>) memberValuesField.get(handler);
            String[] originalPlugins = (String[]) memberValues.get("plugin");

            List<String> newPlugins = originalPlugins == null ? new ArrayList<>() : new ArrayList(Arrays.asList(originalPlugins));
            if (newPlugins.contains(OCTANE_FORMATTER)) {
                return;
            }
            newPlugins.add(OCTANE_FORMATTER);
            String[] result = new String[newPlugins.size()];
            memberValues.put("plugin", newPlugins.toArray(result));
        } catch (Exception e) {
            System.out.println("Failed to add formatter to plugin: " + e);
        }
    }
}
