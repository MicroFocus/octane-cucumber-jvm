package com.hpe.alm.octane.infra;

import cucumber.api.Result;
import gherkin.ast.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StepElement implements GherkinSerializer {
    private String name;
    private String keyword;
    private String status = Result.Type.SKIPPED.lowerCaseName();
    private Integer line = 0;
    private Long duration = 0L;
    private String errorMessage;
    private boolean isBackgroundStep = false;

    public StepElement(String name, String keyword, Integer line){
        this.name = name;
        this.keyword = keyword;
        this.line = line;
    }

    public StepElement(Step step) {
        if(step != null){
            name = step.getKeyword() + step.getText();
            line = step.getLocation().getLine();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBackgroundStep() {
        isBackgroundStep = true;
    }

    public boolean isBackgroundStep() {
        return isBackgroundStep;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStatus(String status) {
        this.status = status.toLowerCase();
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public Integer getLine() {
        return line;
    }

    public String getName() {
        return name;
    }

    public String getKeyword() {
        return keyword;
    }

    public Element toXMLElement(Document doc) {
        Element step = doc.createElement(STEP_TAG_NAME);

        step.setAttribute("name", keyword + name);
        step.setAttribute("status", status);

        String duration = this.duration != null ? this.duration.toString() : "0";
        step.setAttribute("duration", duration);

        //temporarily set error message only for non-background steps
        if(errorMessage!=null && !errorMessage.isEmpty() && !isBackgroundStep){
            Element errorElement = doc.createElement(GherkinSerializer.ERROR_MESSAGE_TAG_NAME);
            errorElement.appendChild(doc.createCDATASection(errorMessage));
            step.appendChild(errorElement);
        }

        return step;
    }
}