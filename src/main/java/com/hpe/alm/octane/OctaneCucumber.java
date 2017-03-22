package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.OutputFile;
import cucumber.api.junit.Cucumber;
import cucumber.runtime.Runtime;
import cucumber.runtime.RuntimeOptions;
import cucumber.runtime.io.ResourceLoader;
import org.junit.runners.model.InitializationError;

import java.io.IOException;

public class OctaneCucumber extends Cucumber {

    public OctaneCucumber(Class clazz) throws IOException, InitializationError {
        super(clazz);
    }

    protected Runtime createRuntime(ResourceLoader resourceLoader, ClassLoader classLoader, RuntimeOptions runtimeOptions) throws InitializationError, IOException {
        OutputFile outputFile = new OutputFile(this.getTestClass().getJavaClass());
        runtimeOptions.addPlugin(new HPEAlmOctaneGherkinFormatter(resourceLoader, runtimeOptions.getFeaturePaths(), outputFile));
        return super.createRuntime(resourceLoader, classLoader, runtimeOptions);
    }
}
