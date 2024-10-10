package com.hpe.alm.octane.infra;


import io.cucumber.gherkin.GherkinDialect;
import io.cucumber.gherkin.GherkinDialectProvider;
import io.cucumber.gherkin.GherkinParser;
import io.cucumber.messages.types.*;
import io.cucumber.plugin.event.Node;
import io.cucumber.plugin.event.TestSourceRead;

import java.lang.Exception;
import java.util.*;

import static io.cucumber.messages.types.SourceMediaType.TEXT_X_CUCUMBER_GHERKIN_PLAIN;

public class TestSourcesModel {
    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap();
    private final Map<String, GherkinDocument> pathToGherkinDocument = new HashMap();

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
                        return;
                    }
                } else {
                    ErrorHandler.error("Failed to parse gherkin source. gherkin doc env is empty");
                    return;
                }

                if(gherkinDocumentEnv.get().getGherkinDocument().isEmpty()){
                    ErrorHandler.error("Failed to parse gherkin source. Gherkin document is empty");
                    return;
                }
                pathToGherkinDocument.put(path,gherkinDocumentEnv.get().getGherkinDocument().get());
            } catch (Exception e) {
                ErrorHandler.error("Failed to parse gherkin source", e);
            }
        }
    }
}
