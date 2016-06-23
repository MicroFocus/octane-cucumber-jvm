import gherkin.formatter.model.Step;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

class StepElement implements GherkinSerializer {
    private String _name = "";
    private String _status = "";
    private Integer _line = 0;
    private Long _duration = (long)0;

    StepElement(Step step) {
        if(step!=null){
            _name = step.getKeyword() + step.getName();
            _line = step.getLine();
        }
    }

    void setStatus(String status) {
        this._status = status;
    }

    void setDuration(Long duration) {
        this._duration = duration;
    }

    Integer getLine() {
        return _line;
    }

    public Element toXMLElement(Document doc) {
        Element step = doc.createElement(STEP_TAG_NAME);

        step.setAttribute("name", _name);
        if (_status != null && !_status.isEmpty()) {
            step.setAttribute("status", _status);
        }

        String duration = _duration != null ? _duration.toString() : "0";
        step.setAttribute("duration", duration);

        return step;
    }
}