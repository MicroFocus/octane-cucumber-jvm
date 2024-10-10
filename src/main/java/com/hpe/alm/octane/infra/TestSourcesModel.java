package com.hpe.alm.octane.infra;


import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.*;
import io.cucumber.plugin.event.TestSourceRead;

import java.lang.Exception;
import java.util.*;

import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;

public class TestSourcesModel {
    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap();
    private final Map<String, GherkinDocument> pathToGherkinDocument = new HashMap();
    private final Map<Long, Scenario> line2Scenario = new HashMap<>();

    public void addTestSourceReadEvent(String path, TestSourceRead event) {
        pathToReadEventMap.put(path, event);
    }

    Optional<Feature> getFeature(String path) {
        if (!pathToGherkinDocument.containsKey(path)) {
            parseGherkinSource(path);
        }

        return pathToGherkinDocument.containsKey(path) ? pathToGherkinDocument.get(path).getFeature() : Optional.empty();
    }

    public String getKeywordFromSource(String uri, int stepLine) {
        Optional<Feature> feature = getFeature(uri);
        if (feature.isPresent()) {
            TestSourceRead event = getTestSourceReadEvent(uri);
            String trimmedSourceLine = event.getSource().split("\n")[stepLine - 1].trim();
            GherkinDialect dialect = (new GherkinDialectProvider(feature.get().getLanguage())).getDefaultDialect();
            Iterator keywords = dialect.getStepKeywords().iterator();

            while (keywords.hasNext()) {
                String keyword = (String)keywords.next();
                if (trimmedSourceLine.startsWith(keyword)) {
                    return keyword;
                }
            }
        }

        return "";
    }

    private TestSourceRead getTestSourceReadEvent(String uri) {
        return pathToReadEventMap.getOrDefault(uri, null);
    }

    public String getFeatureFileContent(String uri) {
        return pathToReadEventMap.containsKey(uri) ? pathToReadEventMap.get(uri).getSource() : null;
    }

    public String getFeatureName(String uri) {
        Optional<Feature> feature = getFeature(uri);
        return feature.isPresent() ? feature.get().getName() : "";
    }

    private void parseGherkinSource(String path) {
        if (pathToReadEventMap.containsKey(path)) {
            try {
                Optional<Envelope> gherkinDocumentEnv = getGherkinDocumentEnvelope(path);
                if (gherkinDocumentEnv.isEmpty() || gherkinDocumentEnv.get().getGherkinDocument().isEmpty()) {
                    ErrorHandler.error("Failed to parse gherkin source. Gherkin document is empty");
                    return;
                }
                GherkinDocument gherkinDocument =  gherkinDocumentEnv.get().getGherkinDocument().get();
                pathToGherkinDocument.put(path,gherkinDocument);

                if(gherkinDocument.getFeature().isPresent()) {
                    Feature feature = gherkinDocument.getFeature().get();
                    feature.getChildren().forEach(featureChild -> {
                        if (featureChild.getScenario().isPresent()) {
                            processScenarioDefinition(featureChild.getScenario().get());
                        }
                        if (featureChild.getRule().isPresent()) {
                            Rule rule = featureChild.getRule().get();
                            rule.getChildren().forEach(ruleChild -> {
                                if (ruleChild.getScenario().isPresent()) {
                                    processScenarioDefinition(ruleChild.getScenario().get());
                                }
                            });
                        }
                    });
                }
            } catch (Exception e) {
                ErrorHandler.error("Failed to parse gherkin source", e);
            }
        }
    }

    private void processScenarioDefinition(Scenario scenario){
        if(scenario.getExamples().isEmpty()){
            line2Scenario.put(scenario.getLocation().getLine(), scenario);
        } else {
            //outline scenario
            scenario.getExamples().forEach(example->{
                example.getTableBody().forEach(tableRow -> {
                    line2Scenario.put(tableRow.getLocation().getLine(), scenario);
                });
            });
        }
    }

    public String getScenarioName(io.cucumber.plugin.event.TestCase testCase){
        if(line2Scenario.containsKey(Long.valueOf(testCase.getLocation().getLine()))){
            return line2Scenario.get(Long.valueOf(testCase.getLocation().getLine())).getName();
        }
        return testCase.getName();
    }

    private Optional<Envelope> getGherkinDocumentEnvelope(String path){
        Envelope envelope = Envelope.of(new Source("", pathToReadEventMap.get(path).getSource(), TEXT_X_CUCUMBER_GHERKIN_PLAIN));
        Optional<Envelope> gherkinDocumentEnv = GherkinParser.builder()
                .includeSource(false)
                .includePickles(false)
                .build()
                .parse(envelope)
                .findFirst();
        if (gherkinDocumentEnv.isPresent()) {
            if(gherkinDocumentEnv.get().getParseError().isPresent()) {
                ErrorHandler.error(String.format("The Gherkin script contains errors. Fix them and then try again. %s.", gherkinDocumentEnv.get().getParseError().get().getMessage()));
                return Optional.empty();
            }
        } else {
            ErrorHandler.error("Failed to parse gherkin source. gherkin doc env is empty");
            return Optional.empty();
        }
        return gherkinDocumentEnv;
    }
}
