package io.redlink.more.studymanager.core.webcomponent;

public class WebComponent {
    private String className;
    private String script;

    public WebComponent(String className, String script) {
        this.className = className;
        this.script = script;
    }

    public String getClassName() {
        return className;
    }

    public String getScript() {
        return script;
    }
}
