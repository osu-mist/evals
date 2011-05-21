package edu.osu.cws.pass.models;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.ResourceBundle;

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

    /**
     * Takes in a resource bundle key, the appraisal map and the resource bundle to
     * generate the anchorText used in the required actions.
     *
     * @param key
     * @param appraisalMap
     * @param resource
     */
    public void setAnchorText(String key, HashMap appraisalMap, ResourceBundle resource) {
        String pattern = "";
        String reviewPeriod = MessageFormat.format("{0,date,yyyy}-{1,date,yyyy}",
                new Object[]{appraisalMap.get("startDate"), appraisalMap.get("endDate")});

        if (key.equals("action-required-goals-due") ||
                key.equals("action-required-goals-past-due") ||
                key.equals("action-required-goals-required-modification") ||
                key.equals("action-required-results-due") ||
                key.equals("action-required-results-past-due") ||
                key.equals("action-required-goals-reactivated") ||
                key.equals("action-required-signature-due") ||
                key.equals("action-required-signature-past-due")
            ) {
            pattern = resource.getString(key);
            anchorText = MessageFormat.format(pattern, appraisalMap.get("positionTitle"), reviewPeriod);

        }

        if (key.equals("action-required-goals-approval-due") ||
                key.equals("action-required-goals-approval-past-due") ||
                key.equals("action-required-appraisal-due") ||
                key.equals("action-required-appraisal-past-due") ||
                key.equals("action-required-release-due") ||
                key.equals("action-required-release-past-due") ||
                key.equals("action-required-response-evaluation-due") ||
                key.equals("action-required-response-evaluation-past-due") ||
                key.equals("action-requited-2nd-release-due") ||
                key.equals("action-requited-2nd-release-past-due")
            ) {
            pattern = resource.getString(key);
            anchorText = MessageFormat.format(pattern, appraisalMap.get("employeeName"),
                    reviewPeriod);

        }

        // If the key has not mathced any of the dynamic keys defined before, return the plain
        // text value.
        if (anchorText.equals("")) {
            anchorText = resource.getString(key);
        }
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
}
