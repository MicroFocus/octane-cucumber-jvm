package com.hpe.alm.octane.infra;

import cucumber.api.Result;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class StepElement implements GherkinSerializer {
    private String name;
    private String status = Result.Type.SKIPPED.lowerCaseName();
    private Long duration = 0L;
    private String errorMessage = "";

    public StepElement(String name) {
        this.name = name;
    }

    public void setName(String name) {
        this.name = name;
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

        if(errorMessage != null && !errorMessage.isEmpty()){
            Element errorElement = doc.createElement(GherkinSerializer.ERROR_MESSAGE_TAG_NAME);
            errorElement.appendChild(doc.createCDATASection(errorMessage));
            step.appendChild(errorElement);
        }

        return step;
    }
}