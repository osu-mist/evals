package edu.osu.cws.evals.portlet;


import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.models.Configuration;
import edu.osu.cws.evals.util.EvalsUtil;
import edu.osu.cws.util.CWSUtil;

import java.text.MessageFormat;
import java.util.Date;
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
     * @param appraisal
     * @param resource
     * @param configuration
     */
    public void setAnchorText(String key, Appraisal appraisal, ResourceBundle resource,
                              Configuration configuration) throws Exception {

        String pattern = resource.getString(key);

        Date dueDate = EvalsUtil.getDueDate(appraisal, configuration);
        String dueOn = EvalsUtil.formatDate(dueDate);

        String name = "";
        if (appraisal.getJob() != null && appraisal.getJob().getEmployee() != null) {
            name = appraisal.getJob().getEmployee().getName();
        }
        int numDays = Math.abs(CWSUtil.getRemainDays(dueDate));
        String jobTitle = appraisal.getJob().getJobTitle();
        String reviewPeriod = appraisal.getReviewPeriod();
        boolean isTeamAction = key.contains("action-team");

        if (key.contains("goals-due") || key.contains("goals-overdue") ||
                key.contains("results-due") || key.contains("results-overdue") ||
                key.contains("signature-due") || key.contains("signature-overdue")
                ) {
            if (isTeamAction) {
                anchorText = MessageFormat.format(pattern, name, reviewPeriod, numDays);
            } else {
                anchorText = MessageFormat.format(pattern, jobTitle, reviewPeriod, numDays);
            }
        }

        if (key.contains("goals-required-modification") || key.contains("goals-reactivated")) {
            if (isTeamAction) {
                anchorText = MessageFormat.format(pattern, name, reviewPeriod, dueOn);
            } else {
                anchorText = MessageFormat.format(pattern, jobTitle, reviewPeriod, dueOn);
            }
        }

        if (key.equals("action-required-goals-approval-due") ||
                key.equals("action-required-goals-approval-overdue") ||
                key.equals("action-required-appraisal-due") ||
                key.equals("action-required-appraisal-overdue") ||
                key.equals("action-required-release-due") ||
                key.equals("action-required-release-overdue") ||
                key.equals("action-required-2nd-release-due") ||
                key.equals("action-required-2nd-release-overdue") ||
                key.contains("action-required-rebuttal-read")
            ) {
            pattern = resource.getString(key);
            anchorText = MessageFormat.format(pattern, name, numDays);
        }

        // If the key has not matched any of the dynamic keys defined before, return the plain
        // text value.
        if (anchorText.equals("")) {
            anchorText = resource.getString(key);
        }
    }

    /**
     * Takes in a resource bundle key, the review count and the resource bundle to
     * generate the anchorText used by the reviewer actions.
     *
     * @param key
     * @param reviewCount
     * @param resource
     */
    public void setAnchorText(String key, int reviewCount, ResourceBundle resource) {
        String pattern = resource.getString(key);
        anchorText = MessageFormat.format(pattern, reviewCount);
    }

    public HashMap<String, String> getParameters() {
        return parameters;
    }

    public void setParameters(HashMap<String, String> parameters) {
        this.parameters = parameters;
    }
}
