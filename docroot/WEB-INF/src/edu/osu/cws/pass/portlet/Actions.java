package edu.osu.cws.pass.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.util.AppointmentTypes;
import edu.osu.cws.pass.util.Appraisals;
import edu.osu.cws.pass.util.Criteria;
import edu.osu.cws.pass.util.Employees;
import org.hibernate.HibernateException;

import javax.portlet.*;
import java.text.MessageFormat;
import java.util.*;

/**
 * Actions class used to map user form actions to respective class methods.
 */
public class Actions {
    private static Log _log = LogFactoryUtil.getLog(JSPPortlet.class);

    private Employees employees = new Employees();

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
        request.setAttribute("appointmentTypes", new AppointmentTypes().list());

        // When the criterionAreaId == null means that the user clicks on the Add Criteria
        // link. Otherwise the form was submitted
        if (ParamUtil.getString(request, "criterionAreaId").equals("")) {
            _log.error("Actions.addCriteria setting values for new form");
        } else {
            AppointmentType appointmentType = appointmentTypes.findById(
                    ParamUtil.getInteger(request, "appointmentTypeID")
            );

            criterionArea.setName(ParamUtil.getString(request, "name"));
            criterionArea.setAppointmentType(appointmentType);
            criterionDetail.setDescription(ParamUtil.getString(request, "description"));

            try {
                if (criteriaArea.add(criterionArea, criterionDetail, getLoggedOnUsername(request))) {
                    _log.error("criteriaArea add success");
                    SessionMessages.add(request, "criteria-saved");
                    return listCriteria(request, response, portlet);
                }
            } catch (ModelException e) {
                addErrorsToRequest(request, e.getMessage());
            } catch (HibernateException e) {
                _log.error("Hibernate exception - " + e.getMessage());
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
        int appointmentTypeID = ParamUtil.getInteger(request, "appointmentTypeID", Criteria.DEFAULT_APPOINTMENT_TYPE);

        try {
            request.setAttribute("criteria", new Criteria().list(appointmentTypeID));
        } catch (ModelException e) {
            SessionErrors.add(request, e.getMessage());
        } catch (HibernateException e) {
            _log.error("Hibernate exception - " + e.getMessage());
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
     */
    public String displayHomeView(PortletRequest request, PortletResponse response,
                                  JSPPortlet portlet) {
        _log.error("in displayHomeView");
        String username = getLoggedOnUsername(request);
        Employee employee = employees.findByOnid(username);

        request.setAttribute("myActiveAppraisals",
                appraisals.getAllMyActiveAppraisals(employee.getId()));
        request.setAttribute("myTeamsActiveAppraisals",
                appraisals.getMyTeamsActiveAppraisals(employee.getId()));
        request.setAttribute("reviewer", getReviewer(employee.getId()));
        request.setAttribute("admin", getAdmin(employee.getId()));

        request.setAttribute("requiredActions", getRequiredActions(request));

        return "home-jsp";
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
     * Returns a map with information on the currently logged on user.
     *
     * @param request
     * @return
     */
    private Map getLoggedOnUser(PortletRequest request) {
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
        Map userInfo = getLoggedOnUser(request);

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
        Employee loggedInEmployee = employees.findByOnid(getLoggedOnUsername(request));
        ResourceBundle resource = (ResourceBundle) portletContext.getAttribute("resourceBundle");


        ArrayList<HashMap> myActiveAppraisals = (ArrayList<HashMap>)
                request.getAttribute("myActiveAppraisals");
        requiredActions.addAll(getAppraisalActions(myActiveAppraisals, "employee", resource));

        ArrayList<HashMap> supervisorActions = (ArrayList<HashMap>)
                request.getAttribute("myTeamsActiveAppraisals");
        requiredActions.addAll(getAppraisalActions(supervisorActions, "supervisor", resource));

        reviewer = getReviewer(loggedInEmployee.getId());
        if (reviewer != null) {
            reviewerAction = getReviewerAction(reviewer.getBusinessCenterName());
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
        String anchorText = "";
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
     * @return
     */
    private RequiredAction getReviewerAction(String businessCenterName) {
        //@todo: call getReviewCount(bcName) to get an array of the appraisal objects that are due for review for the business center.
        //	If hibernate getReviewCounts returns 0,
        //	    Return null
        //	Instantiate an RequiredAction object action with the following values:
        //	    anchorText: key=ReviewAction, value in the resource bundle should be "You have {0} appraisals to review", where the {0} is the counts obtained earlier.
        //	    Parameters:
        //   	    Action=displayReviewList
        // 	        bcName=<bcName>
        //	return the action object.

        return new RequiredAction();
    }
}
