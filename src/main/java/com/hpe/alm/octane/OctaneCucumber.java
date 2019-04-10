package com.hpe.alm.octane;

import com.hpe.alm.octane.infra.OutputFile;
import cucumber.api.StepDefinitionReporter;
import cucumber.api.event.TestRunFinished;
import cucumber.api.event.TestRunStarted;
import cucumber.api.junit.Cucumber;
import cucumber.runner.EventBus;
import cucumber.runner.ThreadLocalRunnerSupplier;
import cucumber.runner.TimeService;
import cucumber.runner.TimeServiceEventBus;
import cucumber.runtime.*;
import cucumber.runtime.filter.Filters;
import cucumber.runtime.filter.RerunFilters;
import cucumber.runtime.formatter.PluginFactory;
import cucumber.runtime.formatter.Plugins;
import cucumber.runtime.io.MultiLoader;
import cucumber.runtime.io.ResourceLoader;
import cucumber.runtime.io.ResourceLoaderClassFinder;
import cucumber.runtime.junit.FeatureRunner;
import cucumber.runtime.junit.JUnitOptions;
import cucumber.runtime.model.CucumberFeature;
import cucumber.runtime.model.FeatureLoader;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

public class OctaneCucumber extends Cucumber {
    private final List<FeatureRunner> children = new ArrayList<>();
    private final EventBus bus;
    private final ThreadLocalRunnerSupplier runnerSupplier;
    private final Filters filters;
    private final JUnitOptions junitOptions;

    public OctaneCucumber(Class clazz) throws InitializationError {
        super(clazz);
        ClassLoader classLoader = clazz.getClassLoader();
        ResourceLoader resourceLoader = new MultiLoader(classLoader);
        FeatureLoader featureLoader = new FeatureLoader(resourceLoader);
        RuntimeOptions runtimeOptions = new RuntimeOptionsFactory(clazz).create();
        FeaturePathFeatureSupplier featureSupplier = new FeaturePathFeatureSupplier(featureLoader, runtimeOptions);
        List<CucumberFeature> features = featureSupplier.get();
        ClassFinder classFinder = new ResourceLoaderClassFinder(resourceLoader, classLoader);
        BackendSupplier backendSupplier = new BackendModuleBackendSupplier(resourceLoader, classFinder, runtimeOptions);
        this.bus = new TimeServiceEventBus(TimeService.SYSTEM);
        Plugins plugins = new Plugins(classLoader, new PluginFactory(), bus, runtimeOptions);
        OutputFile outputFile = new OutputFile(getTestClass().getJavaClass());
        plugins.addPlugin(new OctaneGherkinFormatter(resourceLoader, features, outputFile));
        this.runnerSupplier = new ThreadLocalRunnerSupplier(runtimeOptions, bus, backendSupplier);
        RerunFilters rerunFilters = new RerunFilters(runtimeOptions, featureLoader);
        this.filters = new Filters(runtimeOptions, rerunFilters);
        this.junitOptions = new JUnitOptions(runtimeOptions.isStrict(), runtimeOptions.getJunitOptions());
        final StepDefinitionReporter stepDefinitionReporter = plugins.stepDefinitionReporter();

        // Start the run before reading the features.
        // Allows the test source read events to be broadcast properly
        bus.send(new TestRunStarted(bus.getTime()));
        for (CucumberFeature feature : features) {
            feature.sendTestSourceRead(bus);
        }
        runnerSupplier.get().reportStepDefinitions(stepDefinitionReporter);
        addChildren(features);
    }

    private void addChildren(List<CucumberFeature> cucumberFeatures) throws InitializationError {
        for (CucumberFeature cucumberFeature : cucumberFeatures) {
            FeatureRunner featureRunner = new FeatureRunner(cucumberFeature, filters, runnerSupplier, junitOptions);
            if (!featureRunner.isEmpty()) {
                children.add(featureRunner);
            }
        }
    }

    @Override
    public List<FeatureRunner> getChildren() {
        return children;
    }

    @Override
    protected Statement childrenInvoker(RunNotifier notifier) {
        final Statement features = super.childrenInvoker(notifier);
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                features.evaluate();
                bus.send(new TestRunFinished(bus.getTime()));
            }
        };
    }
}
