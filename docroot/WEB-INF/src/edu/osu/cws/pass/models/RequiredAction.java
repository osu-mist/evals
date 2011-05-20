package edu.osu.cws.pass.models;


import java.util.HashMap;

/**
 * Simple POJO bean used to store information required to build a required
 * action link in the home view of the PASS portlet. This class does not
 * interact with Hibermate.
 */
public class RequiredAction {
    /**
     * This is the text displayed in the jsp files when rendering an action
     * required link.
     */
    private String anchorText;

    /**
     * HashMap of attributes needed to specify in the PortletURL.
     */
    private HashMap<String, String> parameters;

    public RequiredAction() { }

    public String getAnchorText() {
        return anchorText;
    }

    public void setAnchorText(String anchorText) {
        this.anchorText = anchorText;
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
}
