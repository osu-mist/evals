package edu.osu.cws.evals.portlet;

import edu.osu.cws.evals.models.Appraisal;
import edu.osu.cws.evals.hibernate.AppraisalMgr;
import edu.osu.cws.evals.hibernate.OptOutMgr;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import javax.portlet.PortletRequest;
import javax.portlet.PortletResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.util.List;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;

public class BulkUpdateAction implements ActionInterface {

    private ActionHelper actionHelper;

    private HomeAction homeAction;

    private ErrorHandler errorHandler;

    private static List<Map<String, String>> equalityConditionals = Arrays.asList(
        new HashMap<String, String>() {{
            put("display", "=");
            put("value", "=");
        }},
        new HashMap<String, String>() {{
            put("display", "!=");
            put("value", "!=");
        }},
        new HashMap<String, String>() {{
            put("display", "Like");
            put("value", "LIKE");
        }}
    );
    private static List<Map<String, String>> rangeConditionals = Arrays.asList(
        new HashMap<String, String>() {{
            put("display", "<");
            put("value", "&lt;");
        }},
        new HashMap<String, String>() {{
            put("display", ">");
            put("value", "&gt;");
        }},
        new HashMap<String, String>() {{
            put("display", "=");
            put("value", "=");
        }},
        new HashMap<String, String>() {{
            put("display", "&lt;=");
            put("value", "<=");
        }},
        new HashMap<String, String>() {{
            put("display", "&gt;=");
            put("value", ">=");
        }}
    );
    private static List<String> statusOptions = Arrays.asList(
        "",
        Appraisal.STATUS_APPRAISAL_DUE,
        Appraisal.STATUS_APPRAISAL_OVERDUE,
        Appraisal.STATUS_ARCHIVED_CLOSED,
        Appraisal.STATUS_ARCHIVED_COMPLETED,
        Appraisal.STATUS_BACK_ORIG_STATUS,
        Appraisal.STATUS_CLOSED,
        Appraisal.STATUS_COMPLETED,
        Appraisal.STATUS_GOALS_APPROVAL_DUE,
        Appraisal.STATUS_GOALS_APPROVAL_OVERDUE,
        Appraisal.STATUS_GOALS_APPROVED,
        Appraisal.STATUS_GOALS_DUE,
        Appraisal.STATUS_GOALS_OVERDUE,
        Appraisal.STATUS_GOALS_REACTIVATED,
        Appraisal.STATUS_GOALS_REACTIVATION_REQUESTED,
        Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION,
        Appraisal.STATUS_REBUTTAL_READ_DUE,
        Appraisal.STATUS_REBUTTAL_READ_OVERDUE,
        Appraisal.STATUS_RELEASE_DUE,
        Appraisal.STATUS_RELEASE_OVERDUE,
        Appraisal.STATUS_RESULTS_DUE,
        Appraisal.STATUS_RESULTS_OVERDUE,
        Appraisal.STATUS_REVIEW_DUE,
        Appraisal.STATUS_REVIEW_OVERDUE,
        Appraisal.STATUS_SIGNATURE_DUE,
        Appraisal.STATUS_SIGNATURE_OVERDUE,
        Appraisal.STATUS_IN_REVIEW,
        Appraisal.STATUS_EMPLOYEE_REVIEW_DUE
    );
    private static List<String> activeStatus = Arrays.asList(
        "'" + Appraisal.STATUS_APPRAISAL_DUE + "'",
        "'" + Appraisal.STATUS_APPRAISAL_OVERDUE + "'",
        "'" + Appraisal.STATUS_BACK_ORIG_STATUS + "'",
        "'" + Appraisal.STATUS_GOALS_APPROVAL_DUE + "'",
        "'" + Appraisal.STATUS_GOALS_APPROVAL_OVERDUE + "'",
        "'" + Appraisal.STATUS_GOALS_APPROVED + "'",
        "'" + Appraisal.STATUS_GOALS_DUE + "'",
        "'" + Appraisal.STATUS_GOALS_OVERDUE + "'",
        "'" + Appraisal.STATUS_GOALS_REACTIVATED + "'",
        "'" + Appraisal.STATUS_GOALS_REACTIVATION_REQUESTED + "'",
        "'" + Appraisal.STATUS_GOALS_REQUIRED_MODIFICATION + "'",
        "'" + Appraisal.STATUS_REBUTTAL_READ_DUE + "'",
        "'" + Appraisal.STATUS_REBUTTAL_READ_OVERDUE + "'",
        "'" + Appraisal.STATUS_RELEASE_DUE + "'",
        "'" + Appraisal.STATUS_RELEASE_OVERDUE + "'",
        "'" + Appraisal.STATUS_RESULTS_DUE + "'",
        "'" + Appraisal.STATUS_RESULTS_OVERDUE + "'",
        "'" + Appraisal.STATUS_REVIEW_DUE + "'",
        "'" + Appraisal.STATUS_REVIEW_OVERDUE + "'",
        "'" + Appraisal.STATUS_SIGNATURE_DUE + "'",
        "'" + Appraisal.STATUS_SIGNATURE_OVERDUE + "'",
        "'" + Appraisal.STATUS_IN_REVIEW + "'",
        "'" + Appraisal.STATUS_EMPLOYEE_REVIEW_DUE + "'"
    );

    private static List<Map<String, String>> FILTERS = Arrays.asList(
        new HashMap<String, String>() {{
            put("field", "endDate");
            put("type", "date");
            put("conditional", "endDateConditional");
            put("value", "endDateValue");
        }},
        new HashMap<String, String>() {{
            put("field", "startDate");
            put("type", "date");
            put("conditional", "startDateConditional");
            put("value", "startDateValue");
        }},
        new HashMap<String, String>() {{
            put("field", "job.orgCodeDescription");
            put("type", "string");
            put("conditional", "orgCodeConditional");
            put("value", "orgCodeValue");
        }},
        new HashMap<String, String>() {{
            put("field", "job.appointmentType");
            put("type", "string");
            put("conditional", "appointmentTypeConditional");
            put("value", "appointmentTypeValue");
        }},
        new HashMap<String, String>() {{
            put("field", "status");
            put("type", "checkbox");
            put("conditional", "activeAppraisalsConditional");
            put("value", "activeAppraisalsValue");
        }}
    );
    private static List<String> UPDATES = Arrays.asList(
        "statusUpdate",
        "optOutUpdate",
        "optOutBoolean"
    );

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

        actionHelper.addToRequestMap("rangeConditionals", rangeConditionals);
        actionHelper.addToRequestMap("equalityConditionals", equalityConditionals);
        actionHelper.addToRequestMap("statusOptions", statusOptions);
        actionHelper.useMaximizedMenu();

        return Constants.JSP_BULK_UPDATE;
    }

    /**
     * 
     *
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    public String filter(PortletRequest request, PortletResponse response) throws Exception {
        if (isAnyFilterSet(request)) {
            String query = "SELECT new edu.osu.cws.evals.models.Appraisal "
                + "("
                +    "id, job.jobTitle, job.positionNumber, "
                +    "startDate, endDate, type, job.employee.id, job.employee.lastName, "
                +    "job.employee.firstName, evaluationSubmitDate, status, "
                +    "job.businessCenterName, job.orgCodeDescription, job.suffix, overdue, "
                +    "job.tsOrgCode, job.appointmentType"
                + ") "
                + "FROM edu.osu.cws.evals.models.Appraisal "
                + "WHERE ";
            
            boolean isFirstConditional = true;
            for (Map<String, String> fieldMap : FILTERS) {
                String field = fieldMap.get("field");
                String value = request.getParameter(fieldMap.get("value"));
                String conditional = request.getParameter(fieldMap.get("conditional"));
                if (!StringUtils.isEmpty(request.getParameter(fieldMap.get("value"))) && !StringUtils.isEmpty(request.getParameter(fieldMap.get("conditional")))) {
                    // add values to request map so previous selections are still visible in the form
                    actionHelper.addToRequestMap(fieldMap.get("value"), value);
                    actionHelper.addToRequestMap(fieldMap.get("conditional"), conditional);

                    String formattedValue;
                    if ("date".equals(fieldMap.get("type"))) {
                        Date date = new SimpleDateFormat("yyyy-MM-dd").parse(value);
                        formattedValue = "'" + new SimpleDateFormat("dd-MMMMM-yyyy").format(date) + "'"; 
                    } else if ("status".equals(field)) {
                        formattedValue = "(" + String.join(", ", activeStatus) + ")";
                    } else {
                        formattedValue = "'" + request.getParameter(fieldMap.get("value")) + "'";
                    }
                    if (isFirstConditional) {
                        isFirstConditional = false;
                    } else {
                        query += "AND ";
                    }
                    query += fieldMap.get("field") + " " + request.getParameter(fieldMap.get("conditional")) + " " + formattedValue + " ";
                }
            }

            Session session = HibernateUtil.getCurrentSession();
            List<Appraisal> appraisals = (List<Appraisal>) session.createQuery(query)
                .list();

            // String appraisalIds = "";
            // for (Appraisal app : appraisals) {
                // app.getJob().getEmployee().getFirstName();
                // if (StringUtils.isEmpty(appraisalIds)) {
                    // appraisalIds += app.getId();
                // } else {
                    // appraisalIds += ", " + app.getId();
                // }
            // }

            actionHelper.addToRequestMap("appraisals", appraisals);
            // actionHelper.addToRequestMap("appraisalIds", appraisalIds);
            actionHelper.getPortletContext().setAttribute("bulkUpdateAppraisals", appraisals);
        }

        return list(request, response);
    }

    private boolean isAnyFilterSet(PortletRequest request) {
        for (Map<String, String> fieldMap : FILTERS) {
            if (!StringUtils.isEmpty(request.getParameter(fieldMap.get("value"))) && !StringUtils.isEmpty(request.getParameter(fieldMap.get("conditional")))) {
                return true;
            }
        }

        return false;
    }

    public String update(PortletRequest request, PortletResponse response) throws Exception {
        List<Appraisal> appraisals = (List<Appraisal>)actionHelper.getPortletContext().getAttribute("bulkUpdateAppraisals");

        if (appraisals != null && isAnyUpdateSet(request)) {
            if (!StringUtils.isEmpty(request.getParameter("statusUpdate"))) {
                String statusUpdate = request.getParameter("statusUpdate");
                actionHelper.addToRequestMap("statusUpdate", statusUpdate);

                List<Appraisal> updatedApps = new ArrayList<Appraisal>();
                for (Appraisal app : appraisals) {
                    Appraisal updatedApp = AppraisalMgr.getAppraisal(app.getId());
                    updatedApp.setStatus(statusUpdate);
                    updatedApps.add(updatedApp);
                }

                boolean result = AppraisalMgr.bulkUpdateAppraisals(updatedApps);
                if (result) {
                    actionHelper.getPortletContext().removeAttribute("bulkUpdateAppraisals");
                    actionHelper.addToRequestMap("updateResult", "Success");
                } else {
                    actionHelper.addToRequestMap("updateResult", "Failure");
                    actionHelper.addToRequestMap("appraisals", appraisals);
                }
            }
            if (!StringUtils.isEmpty(request.getParameter("optOutUpdate")) && !StringUtils.isEmpty(request.getParameter("optOutBoolean"))) {
                String optOutUpdate = request.getParameter("optOutUpdate");
                boolean optOutBoolean = "On".equals(request.getParameter("optOutBoolean")) ? true : false;
                actionHelper.addToRequestMap("optOutUpdate", optOutUpdate);
                actionHelper.addToRequestMap("optOutBoolean", request.getParameter("optOutBoolean"));

                List<String> pidms = new ArrayList<String>();
                for (Appraisal app : appraisals) {
                    pidms.add(String.valueOf(app.getJob().getEmployee().getId()));
                }
                Map<String, Boolean> types = new HashMap<String, Boolean>();
                types.put(optOutUpdate, optOutBoolean);

                Boolean result = OptOutMgr.bulkOptOuts(pidms, types, actionHelper.getLoggedOnUser());
                if (result) {
                    actionHelper.getPortletContext().removeAttribute("bulkUpdateAppraisals");
                    actionHelper.addToRequestMap("updateResult", "Success");
                } else {
                    actionHelper.addToRequestMap("updateResult", "Failure");
                    actionHelper.addToRequestMap("appraisals", appraisals);
                }
            }
        } else {
            actionHelper.getPortletContext().removeAttribute("bulkUpdateAppraisals");
        }

        return list(request, response);
    }

    private boolean isAnyUpdateSet(PortletRequest request) {
        for (String update : UPDATES) {
            if (!StringUtils.isEmpty(request.getParameter(update))) {
                return true;
            }
        }

        return false;
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
