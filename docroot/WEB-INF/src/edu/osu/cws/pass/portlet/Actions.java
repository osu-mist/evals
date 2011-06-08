package edu.osu.cws.pass.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.util.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.portlet.*;
import java.util.*;

/**
 * Actions class used to map user form actions to respective class methods.
 */
public class Actions {
    private static Log _log = LogFactoryUtil.getLog(Actions.class);

    private Employees employees = new Employees();

    private Jobs jobs = new Jobs();

    private AppointmentTypes appointmentTypes = new AppointmentTypes();

    private PortletContext portletContext;

    private Appraisals appraisals = new Appraisals();

    /**
     * Takes the request object and creates POJO objects. Then it calls the respective
     * Hibernate util classes passing the POJOs to handle the saving of data and
     * validation.
     * @param request
     * @param response
     * @param portlet
     * @return jsp
     */
    public String addCriteria(PortletRequest request, PortletResponse response, JSPPortlet portlet) {
        Criteria criteriaArea= new Criteria();
        CriterionArea criterionArea = new CriterionArea();
        CriterionDetail criterionDetail = new CriterionDetail();

        // The processing for this action is done by processAction, we can skip the doView method in the
        // portlet class.
        portlet.skipDoView = true;

        // Fetch list of appointment types to use in add form
        try {
            request.setAttribute("appointmentTypes", new AppointmentTypes().list());
        } catch (Exception e) {
            _log.error("unexpected Exception - " + JSPPortlet.stackTraceString(e));
        }

        // When the criterionAreaId == null means that the user clicks on the Add Criteria
        // link. Otherwise the form was submitted
        if (!ParamUtil.getString(request, "criterionAreaId").equals("")) {
            String appointmentType = ParamUtil.getString(request, "appointmentTypeID");

            criterionArea.setName(ParamUtil.getString(request, "name"));
            criterionArea.setAppointmentType(appointmentType);
            criterionDetail.setDescription(ParamUtil.getString(request, "description"));

            try {
                if (criteriaArea.add(criterionArea, criterionDetail, getLoggedOnUser(request))) {
                    SessionMessages.add(request, "criteria-saved");
                    return listCriteria(request, response, portlet);
                }
            } catch (ModelException e) {
                addErrorsToRequest(request, e.getMessage());
            } catch (HibernateException e) {
                _log.error("Hibernate exception - " + JSPPortlet.stackTraceString(e));
            } catch (Exception e) {
                _log.error("unexpected Exception - " + JSPPortlet.stackTraceString(e));
            }
        }

        request.setAttribute("criterionArea", criterionArea);
        request.setAttribute("criterionDetail", criterionDetail);

        return "criteria-add-jsp";
    }

    /**
     * Takes the request object, fetches POJO object using hibernate. Sets new fields
     * using setter methods on POJO. Calls hibernate method to save data back to db.
     *
     * @param actionRequest
     * @param actionResponse
     */
    public void editCritera(ActionRequest actionRequest, ActionResponse actionResponse) {
//        @todo: takes the request object, fetches the
    }

    /**
     * Takes the request object and passes the employeeType to the hibernate util class.
     * It returns an array of CriterionArea POJO.
     *
     * @param request
     * @param response
     * @return jsp
     */
    public String listCriteria(PortletRequest request, PortletResponse response, JSPPortlet portlet) {
        String appointmentType = ParamUtil.getString(request, "appointmentType", Criteria.DEFAULT_APPOINTMENT_TYPE);

        try {
            request.setAttribute("criteria", new Criteria().list(appointmentType));
        } catch (ModelException e) {
            addErrorsToRequest(request, e.getMessage());
        } catch (HibernateException e) {
            _log.error("Hibernate exception - " + JSPPortlet.stackTraceString(e));
        } catch (Exception e) {
            _log.error("unexpected Exception - " + JSPPortlet.stackTraceString(e));
        }

        return "criteria-list-jsp";
    }

    /**
     * Takes the request object and uses the CriterionAreaID to call the hibernate util
     * and have it delete the CriterionArea.
     *
     * @param actionRequest
     * @param actionResponse
     */
    public void deleteCriteria(ActionRequest actionRequest, ActionResponse actionResponse) {

    }

    /**
     * This method uses the request object to get a string with the new order. It then calls
     * a method in the hibernate util class to update the sequence of criterion for the given
     * employeeType.
     *
     * @param actionRequest
     * @param actionResponse
     */
    public void updateCriteriaSequence(ActionRequest actionRequest, ActionResponse actionResponse) {

    }

    /**
     * Takes care of grabbing all the information needed to display the home view sections
     * (req. actions, my appraisals, my team, reviews and admins) and sets the information
     * in the request object.
     *
     * @param request
     * @param response
     * @param portlet
     * @return
     */
    public String displayHomeView(PortletRequest request, PortletResponse response,
                                  JSPPortlet portlet) {
        Employee employee = getLoggedOnUser(request);

        try {
            request.setAttribute("myActiveAppraisals",
                    appraisals.getAllMyActiveAppraisals(employee.getId()));
            if (jobs.isSupervisor(employee.getId())) {
                request.setAttribute("myTeamsActiveAppraisals",
                        appraisals.getMyTeamsActiveAppraisals(employee.getId()));
                request.setAttribute("isSupervisor", true);
            } else {
                request.setAttribute("isSupervisor", false);
            }
        } catch (Exception e) {
            _log.error("unexpected Exception - " + JSPPortlet.stackTraceString(e));
        }

        request.setAttribute("reviewer", getReviewer(employee.getId()));
        request.setAttribute("admin", getAdmin(employee.getId()));

        request.setAttribute("requiredActions", getRequiredActions(request));


        return "home-jsp";
    }

    /**
     * Handles displaying a list of pending reviews for a given business center.
     *
     * @param request
     * @param response
     * @param portlet
     * @return
     */
    public String displayReviewList(PortletRequest request, PortletResponse response,
                                  JSPPortlet portlet) {
        String businessCenterName = ParamUtil.getString(request, "businessCenterName");
        ArrayList<HashMap> reviews = new ArrayList<HashMap>();
        try {
            reviews = appraisals.getReviews(businessCenterName);
        } catch (Exception e) {
            _log.error("unexpected Exception - " + JSPPortlet.stackTraceString(e));
        }
        request.setAttribute("reviews", reviews);

        return "review-list-jsp";
    }

    /**
     * Handles displaying the appraisal when a user clicks on it. It loads the appraisal
     * object along with the respective permissionRule.
     *
     * @param request
     * @param response
     * @param portlet
     * @return jsp file to render
     */
    public String displayAppraisal(PortletRequest request, PortletResponse response,
                                  JSPPortlet portlet) throws Exception {
        Appraisal appraisal = new Appraisal();
        int appraisalID = ParamUtil.getInteger(request, "id");
        Employee currentlyLoggedOnUser = getLoggedOnUser(request);
        Boolean showForm = false;
        PermissionRule permRule = null;

        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            appraisal = appraisals.getAppraisal(appraisalID);
            permRule = getAppraisalPermissionRule(currentlyLoggedOnUser, appraisal);

            // setting the user role so that for demo purposes we can check permissions
            // using role in jsp
            //@todo: remove line below after demo
            request.setAttribute("userRole",
                appraisals.getRole(appraisal, currentlyLoggedOnUser.getId()));
            tx.commit();
        } catch (ModelException e) {
            SessionErrors.add(request, e.getMessage());
        } catch (Exception e) {
            session.close();
            throw e;
        }

        // Check to see if the logged in user has permission to access the appraisal
        if (permRule == null) {
            SessionErrors.add(request, "appraisal-permission-denied");
            return "home-jsp";
        }

        // Set flag whether or not the html form to update the appraisal needs to be displayed
        if (permRule.getSaveDraft() != null && permRule.getSubmit() != null
                && permRule.getRequireModification() != null) {
            showForm = true;
        }

        request.setAttribute("appraisal", appraisal);
        request.setAttribute("permissionRule", permRule);
        request.setAttribute("showForm", showForm);

        return "appraisal-jsp";
    }

    /**
     * Handles updating the appraisal form.
     *
     * @param request
     * @param response
     * @param portlet
     * @return
     */
    public String updateAppraisal(PortletRequest request, PortletResponse response,
                                  JSPPortlet portlet) throws Exception {
        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            _log.error(entry.getKey() + "/" + entry.getValue()[0]);

        }

        int id = ParamUtil.getInteger(request, "id", 0);
        Employee currentlyLoggedOnUser = getLoggedOnUser(request);

        if (id == 0) {
            SessionErrors.add(request, "appraisal-does-not-exist");
            return "home-jsp";
        }

        Session session = HibernateUtil.getCurrentSession();
        try {
            Transaction tx = session.beginTransaction();
            Appraisal appraisal = (Appraisal) session.get(Appraisal.class, id);
            PermissionRule permRule = getAppraisalPermissionRule(currentlyLoggedOnUser, appraisal);

            // Check to see if the logged in user has permission to access the appraisal
            if (permRule == null) {
                SessionErrors.add(request, "appraisal-permission-denied");
                return "home-jsp";
            }
            // update appraisal & assessment fields based on permission rules
            appraisals.setLoggedInUser(currentlyLoggedOnUser);
            setAppraisalFields(request, appraisal, permRule);


            //@todo: validate appraisal

            // save changes to db
            appraisals.updateAppraisal(appraisal);
            tx.commit();

            // If appraisalStep.getEmailType() is not null
                // Send the email  //design in another module, not for the June demo.
        } catch (ModelException e) {

        } catch (Exception e) {
            session.close();
            throw e;
        }

        return displayHomeView(request, response, portlet);
    }

    /**
     * Figures out the current user role in the appraisal and returns the respective permission
     * rule for that user role and action in the appraisal.
     *
     * @param currentlyLoggedOnUser
     * @param appraisal
     * @return
     */
    private PermissionRule getAppraisalPermissionRule(Employee currentlyLoggedOnUser, Appraisal appraisal) {
        HashMap permissionRules = new HashMap();
        String permissionKey = "";
        try {
            String role = appraisals.getRole(appraisal, currentlyLoggedOnUser.getId());
            permissionKey = appraisal.getStatus()+"-"+ role;
            permissionRules = (HashMap) portletContext.getAttribute("permissionRules");
        } catch (Exception e) {
            _log.error("failed to load appraisal role ");

        }
        _log.error(permissionKey);
        return (PermissionRule) permissionRules.get(permissionKey);
    }

    /**
     * Handles updating the appraisal fields in the appraisal and assessment objects.
     *
     * @param request
     * @param appraisal
     * @param permRule
     */
    private void setAppraisalFields(PortletRequest request, Appraisal appraisal, PermissionRule permRule) {
        String paramaterKey = "";

        // Save Goals
        if (permRule.getGoals() != null && permRule.getGoals().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                paramaterKey = "appraisal.goal." + Integer.toString(assessment.getId());
                if (request.getParameter(paramaterKey) != null) {
                    assessment.setGoal(request.getParameter(paramaterKey));
                }
            }
            if (request.getParameter("submit-goals") != null) {
                appraisal.setGoalsSubmitDate(new Date());
            }
            if (request.getParameter("approve-goals") != null) {
                appraisal.setGoalApprovedDate(new Date());
                appraisal.setGoalsApprover(getLoggedOnUser(request));
            }
        }
        // Save newGoals
        if (permRule.getNewGoals() != null && permRule.getNewGoals().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                paramaterKey = "appraisal.newGoal." + Integer.toString(assessment.getId());
                assessment.setNewGoals(request.getParameter(paramaterKey));
            }
        }
        // Save goalComments
        if (permRule.getGoalComments() != null && permRule.getGoalComments().equals("e")) {
            if (request.getParameter("appraisal.goalsComments") != null) {
                appraisal.setGoalsComments(request.getParameter("appraisal.goalsComments"));
            }
        }
        // Save employee results
        if (permRule.getResults() != null && permRule.getResults().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                paramaterKey = "assessment.employeeResult." + Integer.toString(assessment.getId());
                if (request.getParameter(paramaterKey) != null) {
                    assessment.setEmployeeResult(request.getParameter(paramaterKey));
                }
            }
        }
        // Save Supervisor Results
        if (permRule.getSupervisorResults() != null && permRule.getSupervisorResults().equals("e")) {
            for (Assessment assessment : appraisal.getAssessments()) {
                paramaterKey = "assessment.supervisorResult." + Integer.toString(assessment.getId());
                if (request.getParameter(paramaterKey) != null) {
                    assessment.setSupervisorResult(request.getParameter(paramaterKey));
                }
            }
        }
        if (request.getParameter("submit-results") != null) {
            appraisal.setResultSubmitDate(new Date());
        }
        // Save evaluation
        if (permRule.getEvaluation() != null && permRule.getEvaluation().equals("e")) {
            if (request.getParameter("appraisal.evaluation") != null) {
                appraisal.setEvaluation(request.getParameter("appraisal.evaluation"));
            }
            if (request.getParameter("appraisal.rating") != null) {
                appraisal.setRating(Integer.parseInt(request.getParameter("appraisal.rating")));
            }
            if (request.getParameter(permRule.getSubmit()) != null) {
                appraisal.setEvaluationSubmitDate(new Date());
                appraisal.setEvaluator(getLoggedOnUser(request));
            }
        }
        // Save review
        if (permRule.getReview() != null && permRule.getReview().equals("e")) {
            if (request.getParameter("appraisal.review") != null) {
                appraisal.setReview(request.getParameter("appraisal.review"));
            }
            if (request.getParameter("submit-appraisal") != null) {
                appraisal.setReviewer(getLoggedOnUser(request));
                appraisal.setReviewSubmitDate(new Date());
            }
        }
        if (request.getParameter("sign-appraisal") != null) {
            appraisal.setEmployeeSignedDate(new Date());
        }
        if (request.getParameter("release-appraisal") != null) {
            appraisal.setSignatureRequestedDate(new Date());
        }
        // Save employee response
        if (permRule.getEmployeeResponse() != null && permRule.getEmployeeResponse().equals("e")) {
            appraisal.setEmployeeResponse(request.getParameter("appraisal.employeeResponse"));
            if (request.getParameter("submit-response") != null) {
                appraisal.setRespondedDate(new Date());
            }
        }

        // If the appraisalStep object has a new status, update the appraisal object
        AppraisalStep appraisalStep = getAppraisalStepKey(request,
                appraisal.getJob().getAppointmentType(), permRule);
        if (appraisalStep != null &&
                !appraisalStep.getNewStatus().equals(appraisal.getStatus())) {
            _log.error("found appraisalStep "+appraisalStep.toString());
            appraisal.setStatus(appraisalStep.getNewStatus());
            if (request.getParameter("sign-appraisal") != null &&
                    appraisal.getEmployeeResponse() != null &&
                    !appraisal.getEmployeeResponse().equals("")) {
                appraisal.setStatus("rebuttal-submitted");
            }
        }

        //@todo: based on the action submit button pressed, we'll want to set different meatadata fields
        //@todo: don't forget to include other fields such as addedBy, modifiedBy, approvedDate
        // such as reviewerID, goalApproverID, submitDate, etc.
    }

    /**
     * Figures out the appraisal step key for the button that the user pressed when the appraisal
     * form was submitted.
     *
     * @param request
     * @param appointmentType
     * @param permRule
     * @return
     */
    private AppraisalStep getAppraisalStepKey(PortletRequest request, String appointmentType,
                                              PermissionRule permRule) {
        AppraisalStep appraisalStep;
        String appraisalStepKey;
        HashMap appraisalSteps = (HashMap) portletContext.getAttribute("appraisalSteps");
        ArrayList<String> appraisalButtons = new ArrayList<String>();
        if (permRule.getSaveDraft() != null) {
            appraisalButtons.add(permRule.getSaveDraft());
        }
        if (permRule.getRequireModification() != null) {
            appraisalButtons.add(permRule.getRequireModification());
        }
        if (permRule.getSubmit() != null) {
            appraisalButtons.add(permRule.getSubmit());
        }

        for (String button : appraisalButtons) {
            // If this button is the one the user clicked, use it to look up the
            // appraisalStepKey
            if (!ParamUtil.getString(request, button).equals("")) {
                appraisalStepKey = button + "-" + appointmentType;
                appraisalStep = (AppraisalStep) appraisalSteps.get(appraisalStepKey);
                _log.error("appraisalStepKey = "+appraisalStepKey);
                if (appraisalStep != null) {
                    return appraisalStep;
                }
            }
        }

        return null;
    }

    /**
     * Takes an string error message and sets in the session.
     *
     * @param request
     * @param errorMsg
     */
    private void addErrorsToRequest(PortletRequest request, String errorMsg) {
        request.setAttribute("errorMsg", errorMsg);
    }

    /**
     * Returns an Employee object of the currently logged on user. First it looks in
     * the PortletSession if it's not there it fetches the Employee object and stores
     * it there.
     *
     * @param request
     * @return
     */
    private Employee getLoggedOnUser(PortletRequest request) {
        PortletSession session = request.getPortletSession(true);
        Employee loggedOnUser = (Employee) session.getAttribute("loggedOnUser");
        if (loggedOnUser == null) {
            try {
                loggedOnUser = employees.findByOnid(getLoggedOnUsername(request));
            } catch (Exception e) {
                _log.error("unexpected Exception - " + JSPPortlet.stackTraceString(e));
            }
            session.setAttribute("loggedOnUser", loggedOnUser);
        }

        return loggedOnUser;
    }

    /**
     * Returns a map with information on the currently logged on user.
     *
     * @param request
     * @return
     */
    private Map getLoggedOnUserMap(PortletRequest request) {
        Map userInfo = (Map)request.getAttribute(PortletRequest.USER_INFO);
        return userInfo;
    }

    /**
     * Returns the username of the currently logged on user. If there is no valid username, it
     * returns an empty string.
     *
     * @param request
     * @return username
     */
    private String getLoggedOnUsername(PortletRequest request) {
        Map userInfo = getLoggedOnUserMap(request);

        return (userInfo == null) ? "" : (String) userInfo.get("user.login.id");
    }

    public void setPortletContext(PortletContext portletContext) {
        this.portletContext = portletContext;
    }

    /**
     * Takes in a pidm, and looks up in the reviewers HashMap stored in the portlet context
     * to figure out if the current logged in user is a reviewer. If yes, then we return the
     * Reviewer object if not, it returns false.
     *
     * @param pidm  Pidm of currently logged in user
     * @return Reviewer
     */
    private Reviewer getReviewer(int pidm) {
        HashMap<Integer, Reviewer> reviewerMap =
                (HashMap<Integer, Reviewer>) portletContext.getAttribute("reviewers");

        if (reviewerMap.containsKey(pidm)) {
            return reviewerMap.get(pidm);

        }
        return null;
    }

    /**
     * Takes in a pidm, and looks up in the admins HashMap stored in the portlet context
     * to figure out if the current logged in user is a reviewer. If yes, then we return the
     * Admin object if not, it returns false.
     *
     * @return Admin
     */
    private Admin getAdmin(int pidm) {
        HashMap<Integer, Admin> adminMap =
                (HashMap<Integer, Admin>) portletContext.getAttribute("admins");

        if (adminMap.containsKey(pidm)) {
            return adminMap.get(pidm);
        }
        return null;
    }

    /**
     * Using the request object, it fetches the list of employee appraisals and supervisor
     * appraisals and finds out if there are any actions required for them. It also checks
     * to see if the user is a reviewer and it gets the action required for the reviewer.
     *
     * @param request
     */
    public ArrayList<RequiredAction> getRequiredActions(PortletRequest request) {
        ArrayList<RequiredAction> requiredActions = new ArrayList<RequiredAction>();
        RequiredAction reviewerAction;
        Reviewer reviewer;
        Employee loggedInEmployee = getLoggedOnUser(request);
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");


        ArrayList<HashMap> myActiveAppraisals = (ArrayList<HashMap>)
                request.getAttribute("myActiveAppraisals");
        requiredActions.addAll(getAppraisalActions(myActiveAppraisals, "employee", resource));

        // add supervisor required actions, if user has team's active appraisals
        if (request.getAttribute("myTeamsActiveAppraisals") != null) {
            ArrayList<HashMap> supervisorActions = (ArrayList<HashMap>)
                    request.getAttribute("myTeamsActiveAppraisals");
            requiredActions.addAll(getAppraisalActions(supervisorActions, "supervisor", resource));
        }

        reviewer = getReviewer(loggedInEmployee.getId());
        if (reviewer != null) {
            reviewerAction = getReviewerAction(reviewer.getBusinessCenterName(), resource);
            if (reviewerAction != null) {
                requiredActions.add(reviewerAction);
            }
        }
        return requiredActions;
    }


    /**
     * Returns a list of actions required for the given user and role, based on the
     * list of appraisals passed in. If the user and role have no appraisal actions,
     * it returns an empty ArrayList.
     *
     * @param appraisalList     List of appraisals to check for actions required
     * @param role              Role of the currently logged in user
     * @param resource          Resource bundle to pass in to RequiredAction bean
     * @return  outList
     */
    public ArrayList<RequiredAction> getAppraisalActions(List<HashMap> appraisalList,
                                                         String role, ResourceBundle resource) {
        HashMap permissionRuleMap = (HashMap) portletContext.getAttribute("permissionRules");
        ArrayList<RequiredAction> outList = new ArrayList<RequiredAction>();
        String actionKey = "";
        RequiredAction actionReq;
        HashMap<String, String> anchorParams;

        for (HashMap<String, String> appraisalMap : appraisalList) {
            //get the status, compose the key "status"-"role"
            actionKey = appraisalMap.get("status")+"-"+role;

            // Get the appropriate permissionrule object from the permissionRuleMap
            PermissionRule rule = (PermissionRule) permissionRuleMap.get(actionKey);
            if (rule != null && rule.getActionRequired() != null
                    && !rule.getActionRequired().equals("")) {
                // compose a requiredAction object and add it to the outList.
                anchorParams = new HashMap<String, String>();
                anchorParams.put("action", "displayAppraisal");
                anchorParams.put("id", appraisalMap.get("id"));

                actionReq = new RequiredAction();
                actionReq.setParameters(anchorParams);
                actionReq.setAnchorText(rule.getActionRequired(), appraisalMap, resource);
                outList.add(actionReq);
            }
        }
        return outList;
    }

    /**
     * Returns the required action for the business center reviewer.
     *
     * @param businessCenterName
     * @param resource
     * @return
     */
    private RequiredAction getReviewerAction(String businessCenterName, ResourceBundle resource) {
        int reviewCount = 0;
        try {
            reviewCount = appraisals.getReviewCount(businessCenterName);
        } catch (Exception e) {
            _log.error("unexpected Exception - " + JSPPortlet.stackTraceString(e));
        }
        RequiredAction requiredAction = new RequiredAction();
        if (reviewCount == 0) {
            return null;
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("action", "displayReviewList");
        parameters.put("businessCenterName", businessCenterName);
        requiredAction.setAnchorText("action-required-review", reviewCount, resource);
        requiredAction.setParameters(parameters);

        return requiredAction;
    }
}
