package com.hpe.alm.octane.infra;

import cucumber.api.event.TestSourceRead;
import cucumber.runtime.CucumberException;
import gherkin.*;
import gherkin.ast.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class TestSourcesModel {
    private final Map<String, TestSourceRead> pathToReadEventMap = new HashMap();
    private final Map<String, GherkinDocument> pathToAstMap = new HashMap();
    private final Map<String, Map<Integer, TestSourcesModel.AstNode>> pathToNodeMap = new HashMap();

    public void addTestSourceReadEvent(String path, TestSourceRead event) {
        pathToReadEventMap.put(path, event);
    }

    Feature getFeature(String path) {
        if (!pathToAstMap.containsKey(path)) {
            parseGherkinSource(path);
        }

        return pathToAstMap.containsKey(path) ? pathToAstMap.get(path).getFeature() : null;
    }

    public String getKeywordFromSource(String uri, int stepLine) {
        Feature feature = getFeature(uri);
        if (feature != null) {
            TestSourceRead event = getTestSourceReadEvent(uri);
            String trimmedSourceLine = event.source.split("\n")[stepLine - 1].trim();
            GherkinDialect dialect = (new GherkinDialectProvider(feature.getLanguage())).getDefaultDialect();
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
        return pathToReadEventMap.containsKey(uri) ? pathToReadEventMap.get(uri).source : null;
    }

    public String getFeatureName(String uri) {
        Feature feature = getFeature(uri);
        return feature != null ? feature.getName() : "";
    }

    private void parseGherkinSource(String path) {
        if (pathToReadEventMap.containsKey(path)) {
            Parser<GherkinDocument> parser = new Parser(new AstBuilder());
            TokenMatcher matcher = new TokenMatcher();

            try {
                GherkinDocument gherkinDocument = parser.parse(pathToReadEventMap.get(path).source, matcher);
                pathToAstMap.put(path, gherkinDocument);
                Map<Integer, TestSourcesModel.AstNode> nodeMap = new HashMap();
                TestSourcesModel.AstNode currentParent = new TestSourcesModel.AstNode(gherkinDocument.getFeature(), null);
                Iterator scenarioDefinitionIterator = gherkinDocument.getFeature().getChildren().iterator();

                while (scenarioDefinitionIterator.hasNext()) {
                    ScenarioDefinition child = (ScenarioDefinition)scenarioDefinitionIterator.next();
                    processScenarioDefinition(nodeMap, child, currentParent);
                }

                pathToNodeMap.put(path, nodeMap);
            } catch (ParserException e) {
                ErrorHandler.error("Failed to parse gherkin source", e);
            }

        }
    }

    private void processScenarioDefinition(Map<Integer, TestSourcesModel.AstNode> nodeMap, ScenarioDefinition child, TestSourcesModel.AstNode currentParent) {
        TestSourcesModel.AstNode childNode = new TestSourcesModel.AstNode(child, currentParent);
        nodeMap.put(child.getLocation().getLine(), childNode);
        Iterator stepIterator = child.getSteps().iterator();

        while (stepIterator.hasNext()) {
            Step step = (Step)stepIterator.next();
            nodeMap.put(step.getLocation().getLine(), new TestSourcesModel.AstNode(step, childNode));
        }

        if (child instanceof ScenarioOutline) {
            processScenarioOutlineExamples(nodeMap, (ScenarioOutline)child, childNode);
        }

    }

    private void processScenarioOutlineExamples(Map<Integer, TestSourcesModel.AstNode> nodeMap, ScenarioOutline scenarioOutline, TestSourcesModel.AstNode childNode) {
        Iterator examplesIterator = scenarioOutline.getExamples().iterator();

        while (examplesIterator.hasNext()) {
            Examples examples = (Examples)examplesIterator.next();
            TestSourcesModel.AstNode examplesNode = new TestSourcesModel.AstNode(examples, childNode);
            TableRow headerRow = examples.getTableHeader();
            TestSourcesModel.AstNode headerNode = new TestSourcesModel.AstNode(headerRow, examplesNode);
            nodeMap.put(headerRow.getLocation().getLine(), headerNode);

            for(int i = 0; i < examples.getTableBody().size(); ++i) {
                TableRow examplesRow = examples.getTableBody().get(i);
                Node rowNode = new TestSourcesModel.ExamplesRowWrapperNode(examplesRow, i);
                TestSourcesModel.AstNode expandedScenarioNode = new TestSourcesModel.AstNode(rowNode, examplesNode);
                nodeMap.put(examplesRow.getLocation().getLine(), expandedScenarioNode);
            }
        }

    }

    class AstNode {
        final Node node;
        final TestSourcesModel.AstNode parent;

        AstNode(Node node, TestSourcesModel.AstNode parent) {
            this.node = node;
            this.parent = parent;
        }
    }

    class ExamplesRowWrapperNode extends Node {
        final int bodyRowIndex;

        ExamplesRowWrapperNode(Node examplesRow, int bodyRowIndex) {
            super(examplesRow.getLocation());
            this.bodyRowIndex = bodyRowIndex;
        }
    }
}
