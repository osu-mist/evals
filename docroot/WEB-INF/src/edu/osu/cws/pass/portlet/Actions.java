package edu.osu.cws.pass.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.pass.models.AppointmentType;
import edu.osu.cws.pass.models.CriterionArea;
import edu.osu.cws.pass.models.CriterionDetail;
import edu.osu.cws.pass.models.Employee;
import edu.osu.cws.pass.util.AppointmentTypes;
import edu.osu.cws.pass.util.Criteria;
import edu.osu.cws.pass.util.Employees;
import edu.osu.cws.pass.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;

import javax.portlet.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Actions class used to map user form actions to respective class methods.
 */
public class Actions {
    private static Log _log = LogFactoryUtil.getLog(JSPPortlet.class);

    private Employees employees = new Employees();

    private AppointmentTypes appointmentTypes = new AppointmentTypes();

    /**
     * Takes the request object and creates POJO objects. Then it calls the respective
     * Hibernate util classes passing the POJOs to handle the saving of data and
     * validation.
     *
     * @param actionRequest
     * @param actionResponse
     * @return jsp
     */
/*
    public String addCritera(ActionRequest actionRequest, ActionResponse actionResponse) {
//        @todo: takes the request object and create the POJO objects
//        @todo: calls the hibernate method createNewCriterion passing on the POJOs
        return "/jsp/criteria/add.jsp";
    }
*/

    /**
     * This method is called when the user wants to add a Criteria. It is used
     *
     * @param request
     * @param response
     * @param portlet
     * @return jsp
     */
    public String addCriteria(PortletRequest request, PortletResponse response, JSPPortlet portlet) {
        Criteria criteriaArea= new Criteria();
        CriterionArea criterionArea = new CriterionArea();
        CriterionDetail criterionDetail = new CriterionDetail();

        //@todo: remove debug line below
//        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
//            _log.error(entry.getKey() + "/" + entry.getValue()[0]);
//        }

        // Fetch list of appointment types to use in add form
        request.setAttribute("appointmentTypes", new AppointmentTypes().list());

        // When the criterionAreaId == null means that the user clicks on the Add Criteria
        // link. Otherwise the form was submitted
        if (ParamUtil.getString(request, "criterionAreaId").equals("")) {
            _log.error("Actions.addCriteria setting values for new form");
        } else {
            _log.error("Actions.addCriteria trying to save data");
            AppointmentType appointmentType = appointmentTypes.findById(
                    ParamUtil.getInteger(request, "appointmentTypeID")
            );
            Employee createdBy = employees.findByOnid(getLoggedOnUsername(request));

            _log.error("area name = "+ ParamUtil.getString(request, "name"));
            criterionArea.setName(ParamUtil.getString(request, "name"));
            criterionArea.setSequence(criteriaArea.getNextSequence(Criteria.DEFAULT_APPOINTMENT_TYPE));
            criterionArea.setAppointmentTypeID(appointmentType);
            criterionArea.setCreatedBy(createdBy);
            _log.error("description name = " + ParamUtil.getString(request, "description"));
            criterionDetail.setDescription(ParamUtil.getString(request, "description"));
            criterionDetail.setCreatedBy(createdBy);

            if (criteriaArea.add(criterionArea, criterionDetail)) {
                _log.error("criteriaArea add success");
                SessionMessages.add(request, "criteria-saved");
                portlet.skipDoView = false;
                portlet.viewAction = "listCriteria";
                return "/jsp/criteria/list.jsp";
            } else {
                _log.error("criteriaArea add fail");
                this.addErrorsToSession(request, criterionArea.getErrorKeys());
                this.addErrorsToSession(request, criterionDetail.getErrorKeys());
            }
        }

        request.setAttribute("criterionArea", criterionArea);
        request.setAttribute("criterionDetail", criterionDetail);

        return "/jsp/criteria/add.jsp";
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

        List foo = new Criteria().list(appointmentTypeID);
        request.setAttribute("criteria", foo);

        return "/jsp/criteria/list.jsp";
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
     * Takes an ArrayList of errors and adds them to the session as errors.
     *
     * @param request
     * @param errorKeys
     */
    private void addErrorsToSession(PortletRequest request, ArrayList<String> errorKeys) {
        for (String errorKey : errorKeys) {
            _log.error("adding errorKey: "+errorKey);
            SessionErrors.add(request, "error-"+errorKey);
        }
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
}
