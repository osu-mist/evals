package edu.osu.cws.util;

public class Breadcrumb {
    private String anchorText;
    private String scope;
    private String scopeValue;

    public Breadcrumb(String anchorText, String scope, String scopeValue) {
        this.anchorText = anchorText;
        this.scope = scope;
        this.scopeValue = scopeValue;
    }

    public String getAnchorText() {
        return anchorText;
    }

    public void setAnchorText(String anchorText) {
        this.anchorText = anchorText;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getScopeValue() {
        return scopeValue;
    }

    public void setScopeValue(String scopeValue) {
        this.scopeValue = scopeValue;
    }
}
