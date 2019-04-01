package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.OutputFile;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptionsFactory;
import cucumber.runtime.model.CucumberFeature;
import org.junit.runners.model.InitializationError;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class OctaneCucumber extends Cucumber {

    public OctaneCucumber(Class clazz) throws InitializationError {
        super(clazz);
        executeTests(clazz);
    }

    private void executeTests(Class clazz) {
        OutputFile outputFile = new OutputFile(getTestClass().getJavaClass());
        Runtime.Builder builder = Runtime.builder();
        List<CucumberFeature> cucumberFeatures = new ArrayList<>();
        getChildren().forEach(featureRunner -> {
            Field cucumberFeatureField;
            try {
                cucumberFeatureField = featureRunner.getClass().getDeclaredField("cucumberFeature");
                cucumberFeatureField.setAccessible(true);
                cucumberFeatures.add((CucumberFeature)cucumberFeatureField.get(featureRunner));
                cucumberFeatureField.setAccessible(false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });
        Runtime runtime = builder.withAdditionalPlugins(new OctaneGherkinFormatter(cucumberFeatures, outputFile))
                .withRuntimeOptions(new RuntimeOptionsFactory(clazz).create()).build();
        runtime.run();//todo - where to add the runtime
    }
}
