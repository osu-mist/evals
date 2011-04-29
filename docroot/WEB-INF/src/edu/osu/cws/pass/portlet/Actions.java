package edu.osu.cws.pass.portlet;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.servlet.SessionErrors;
import com.liferay.portal.kernel.servlet.SessionMessages;
import com.liferay.portal.kernel.util.ParamUtil;
import edu.osu.cws.pass.models.*;
import edu.osu.cws.pass.util.AppointmentTypes;
import edu.osu.cws.pass.util.Criteria;
import edu.osu.cws.pass.util.Employees;
import org.hibernate.HibernateException;

import javax.portlet.*;
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
            _log.error("Actions.addCriteria trying to save data");
            _log.error("area name = "+ ParamUtil.getString(request, "name"));
            _log.error("description name = " + ParamUtil.getString(request, "description"));

            AppointmentType appointmentType = appointmentTypes.findById(
                    ParamUtil.getInteger(request, "appointmentTypeID")
            );
            Employee createdBy = employees.findByOnid(getLoggedOnUsername(request));

            criterionArea.setName(ParamUtil.getString(request, "name"));
            criterionArea.setAppointmentTypeID(appointmentType);
            criterionDetail.setDescription(ParamUtil.getString(request, "description"));

            try {
                if (criteriaArea.add(criterionArea, criterionDetail, getLoggedOnUsername(request))) {
                    _log.error("criteriaArea add success");
                    SessionMessages.add(request, "criteria-saved");
                    return listCriteria(request, response, portlet);
                }
            } catch (ModelException e) {
//                SessionErrors.add(request, e.getMessage());
//                request.setAttribute("validation-error", e.getMessage());
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
}
