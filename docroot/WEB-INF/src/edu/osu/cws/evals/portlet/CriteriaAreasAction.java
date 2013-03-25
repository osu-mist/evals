package edu.osu.cws.evals.portlet;

import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.evals.hibernate.AppointmentTypeMgr;
import edu.osu.cws.evals.hibernate.CriteriaMgr;
import edu.osu.cws.evals.models.CriterionArea;
import edu.osu.cws.evals.models.CriterionDetail;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ModelException;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.util.List;

public class CriteriaAreasAction implements ActionInterface {
    
    private ActionHelper actionHelper;

    private HomeAction homeAction;

    /**
     * Takes the request object and passes the employeeType to the hibernate util class.
     * It returns an array of CriterionArea POJO.
     *
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String list(PortletRequest request, PortletResponse response) throws Exception {
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            return ErrorHandler.handleAccessDenied(request, response);
        }

        String appointmentType = ParamUtil.getString(request, "appointmentType",
                CriteriaMgr.DEFAULT_APPOINTMENT_TYPE);

        try {
            List<CriterionArea> criterionList = new CriteriaMgr().list(appointmentType);
            for (CriterionArea criteria : criterionList) {
                criteria.getCurrentDetail().toString();
            }
            actionHelper.addToRequestMap("criteria", criterionList,request);
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(request, e.getMessage());
        }

        actionHelper.useMaximizedMenu(request);
        return Constants.JSP_CRITERIA_LIST;
    }

    /**
     * Takes the request object and creates POJO objects. Then it calls the respective
     * Hibernate util classes passing the POJOs to handle the saving of data and
     * validation.
     * @param request   PortletRequest
     * @param response  PortletResponse
     * @return jsp      JSP file to display (defined in portlet.xml)
     * @throws Exception
     */
    public String add(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            return ErrorHandler.handleAccessDenied(request, response);
        }

        CriteriaMgr criteriaMgrArea = new CriteriaMgr();
        CriterionArea criterionArea = new CriterionArea();
        CriterionDetail criterionDetail = new CriterionDetail();
        Employee loggedOnUser = actionHelper.getLoggedOnUser(request);

        // Fetch list of appointment types to use in add form
        actionHelper.addToRequestMap("appointmentTypes", new AppointmentTypeMgr().list(),request);

        // When the criterionAreaId == null means that the user clicks on the Add Criteria
        // link. Otherwise the form was submitted
        String criterionAreaId = ParamUtil.getString(request, "criterionAreaId");
        if (!criterionAreaId.equals("")) {
            String appointmentType = ParamUtil.getString(request, "appointmentTypeID");
            String name = ParamUtil.getString(request, "name");
            String description = ParamUtil.getString(request, "description");

            criterionArea.setName(name);
            criterionArea.setAppointmentType(appointmentType);
            criterionDetail.setDescription(description);

            try {
                if (criteriaMgrArea.add(criterionArea, criterionDetail, loggedOnUser)) {
                    SessionMessages.add(request, "criteria-saved");
                    return list(request, response);
                }
            } catch (ModelException e) {
                actionHelper.addErrorsToRequest(request, e.getMessage());
            }
        }

        actionHelper.addToRequestMap("criterionArea", criterionArea,request);
        actionHelper.addToRequestMap("criterionDetail", criterionDetail,request);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_CRITERIA_ADD;

    }

    /**
     * Handles editing of an evaluation criteria. Checks user permission. Then calls CriteriaMgr
     * to handle the editing.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String edit(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            return ErrorHandler.handleAccessDenied(request, response);
        }

        CriteriaMgr criteriaMgr = new CriteriaMgr();
        CriterionArea criterionArea = new CriterionArea();
        CriterionDetail criterionDetail = new CriterionDetail();
        try {
            int criterionAreaId = ParamUtil.getInteger(request, "criterionAreaId");
            if (request instanceof RenderRequest) {
                criterionArea = criteriaMgr.get(criterionAreaId);
                if (criterionArea != null) {
                    criterionDetail = criterionArea.getCurrentDetail();
                }
            } else {
                Employee loggedOnUser = actionHelper.getLoggedOnUser(request);
                criteriaMgr.edit(request.getParameterMap(), criterionAreaId, loggedOnUser);
                return list(request, response);
            }
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(request, e.getMessage());
        }

        actionHelper.addToRequestMap("criterionArea", criterionArea,request);
        actionHelper.addToRequestMap("criterionDetail", criterionDetail,request);
        actionHelper.useMaximizedMenu(request);

        return Constants.JSP_CRITERIA_ADD;
    }

    /**
     * Handles deleting an evaluation criteria. If the request a regular http request, it
     * displays a confirm page. Once the user confirms the deletion, the criteria is deleted,
     * the sequence is updated and the list of criteria is displayed again. If the request is
     * AJAX, we remove the evaluation criteria.
     *
     * @param request
     * @param response
     * @return String   If the request is ajax returns json, otherwise jsp file
     * @throws Exception
     */
    public String delete(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            return ErrorHandler.handleAccessDenied(request, response);
        }

        int criteriaID = ParamUtil.getInteger(request, "id");
        CriteriaMgr criteriaMgrArea = new CriteriaMgr();
        try {
            Employee loggedOnUser = actionHelper.getLoggedOnUser(request);

            // If the user clicks on the delete link the first time, use confirm page
            if (request instanceof RenderRequest && response instanceof RenderResponse) {
                CriterionArea criterion = criteriaMgrArea.get(criteriaID);
                actionHelper.addToRequestMap("criterion", criterion,request);
                return Constants.JSP_CRITERIA_DELETE;
            }

            // If user hits cancel, send them to list criteria page
            if (!ParamUtil.getString(request, "cancel").equals("")) {
                return list(request, response);
            }
            criteriaMgrArea.delete(criteriaID, loggedOnUser);
            SessionMessages.add(request, "criteria-deleted");
        } catch (ModelException e) {
            actionHelper.addErrorsToRequest(request, e.getMessage());
            if (actionHelper.isAJAX(request, response)) {
                return e.getMessage();
            }
        }

        if (actionHelper.isAJAX(request, response)) {
            return "success";
        }

        return list(request, response);
    }

    /**
     * This method is called via AJAX when the sequence of an evaluation criteria is updated.
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String updateSequence(PortletRequest request, PortletResponse response) throws Exception {
        // Check that the logged in user is admin
        if (!actionHelper.isLoggedInUserAdmin(request)) {
            return ErrorHandler.handleAccessDenied(request, response);
        }

        int id = ParamUtil.getInteger(request, "id");
        int sequence = ParamUtil.getInteger(request, "sequence");
        CriteriaMgr criteriaMgrArea = new CriteriaMgr();

        try {
            Employee loggedOnUser = actionHelper.getLoggedOnUser(request);
            criteriaMgrArea.updateSequence(id, sequence);
        } catch (ModelException e) {
            return e.getMessage();
        }

        return "success";
    }

    public void setActionHelper(ActionHelper actionHelper) {
        this.actionHelper = actionHelper;
    }

    public void setHomeAction(HomeAction homeAction) {
        this.homeAction = homeAction;
    }
}
