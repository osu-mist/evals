package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.hibernate.JobMgr;
import edu.osu.cws.evals.models.Job;
import edu.osu.cws.evals.models.PositionDescription;
import edu.osu.cws.evals.portlet.ActionHelper;
import edu.osu.cws.evals.portlet.ActionInterface;
import edu.osu.cws.evals.portlet.ErrorHandler;
import edu.osu.cws.evals.portlet.HomeAction;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;

public class PositionDescriptionAction implements ActionInterface {
    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    public String display(PortletRequest request, PortletResponse response) throws Exception {
        AppraisalsAction appraisalsAction = new AppraisalsAction();
        appraisalsAction.setActionHelper(actionHelper);
        appraisalsAction.initialize(request);
        PositionDescription positionDescription = null;

        // Check to see if the logged in user has permission to access the appraisal/position description
        if (appraisalsAction.getPermRule() == null) {
            return errorHandler.handleAccessDenied(request, response);
        }

        // Load the position description
        if (appraisalsAction.getAppraisal() != null) {
            Job job = appraisalsAction.getAppraisal().getJob();
            if (job != null) {
                positionDescription = JobMgr.getPositionDescription(job);
            }
        }

        actionHelper.addToRequestMap("positionDescription", positionDescription);
        return Constants.JSP_POSITION_DESCRIPTION;
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }

    public void setErrorHandler(ErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }
}