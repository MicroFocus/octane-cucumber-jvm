package com.hpe.alm.octane.infra;

import cucumber.api.Result;
import gherkin.ast.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StepElement implements GherkinSerializer {
    private String name = "";
    private String status = Result.Type.SKIPPED.lowerCaseName();
    private Integer line = 0;
    private Long duration = 0L;
    private String errorMessage = "";
    private boolean isBackgroundStep = false;

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

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setStatus(String status) {
        this.status = status.toLowerCase();
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getName() {
        return name;
    }

    public Element toXMLElement(Document doc) {
        Element step = doc.createElement(STEP_TAG_NAME);

        step.setAttribute("name", name);
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