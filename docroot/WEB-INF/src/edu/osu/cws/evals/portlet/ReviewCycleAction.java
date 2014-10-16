package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.CloseOutReasonMgr;
import edu.osu.cws.evals.hibernate.ReviewCycleOptionMgr;
import edu.osu.cws.evals.models.CloseOutReason;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.models.ReviewCycleOption;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.util.ArrayList;

public class ReviewCycleAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    /**
     * Handles listing the close out reasons.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String list(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        boolean isAdmin = actionHelper.getAdmin() != null;
        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }

        ArrayList<ReviewCycleOption> reviewCycleOptions = ReviewCycleOptionMgr.list();
        actionHelper.addToRequestMap("reviewCycleOptions", reviewCycleOptions);
        actionHelper.useMaximizedMenu();

        return Constants.JSP_REVIEW_CYCLE_LIST;
    }

    /**
     * Handles adding a review cycle option. If successful, it displays the list of review cycle options.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String add(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        boolean isAdmin = actionHelper.getAdmin() != null;
        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }

        String reason = ParamUtil.getString(request, "name");
        int value = ParamUtil.getInteger(request, "value");
        int sequence = ParamUtil.getInteger(request, "sequence");

        try {
            ReviewCycleOptionMgr.add(reason, value, sequence, actionHelper.getLoggedOnUser());
            SessionMessages.add(request, "review-cycle-option-added");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(e.getMessage());
        } catch (Exception e) {
            throw e;
        }

        return list(request, response);
    }

    /**
     * Handles performing a soft delete of a single review cycle option.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String delete(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        boolean isAdmin = actionHelper.getAdmin() != null;
        if (!isAdmin) {
            return errorHandler.handleAccessDenied(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        try {
            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                ReviewCycleOption reviewCycleOption = ReviewCycleOptionMgr.get(id);
                actionHelper.addToRequestMap("reviewCycleOption", reviewCycleOption);
                return Constants.JSP_REVIEW_CYCLE_DELETE;
            }

            // If user hits cancel, send them to list admin page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return list(request, response);
            }

            Employee loggedOnUser = actionHelper.getLoggedOnUser();
            ReviewCycleOptionMgr.delete(id, loggedOnUser);
            SessionMessages.add(request, "review-cycle-option-deleted");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(e.getMessage());
        }
        return list(request, response);
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
