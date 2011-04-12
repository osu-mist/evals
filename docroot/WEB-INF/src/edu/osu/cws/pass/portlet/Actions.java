package edu.osu.cws.pass.portlet;

import edu.osu.cws.pass.util.Criteria;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * Actions class used to map user form actions to respective class methods.
 */
public class Actions {
    // criteria, system configuration,

    /**
     * Takes the request object and creates POJO objects. Then it calls the respective
     * Hibernate util classes passing the POJOs to handle the saving of data and
     * validation.
     *
     * @param actionRequest
     * @param actionResponse
     */
    public void addCritera(ActionRequest actionRequest, ActionResponse actionResponse) {
//        @todo: takes the request object and create the POJO objects
//        @todo: calls the hibernate method createNewCriterion passing on the POJOs
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
     * @param renderRequest
     * @param renderResponse
     * @return List
     */
    public void listCriteria(RenderRequest renderRequest, RenderResponse renderResponse) {
        int appointmentTypeID = Criteria.DEFAULT_APPOINTMENT_TYPE;
        if (renderRequest.getParameter("appointmentTypeID") != null) {
            appointmentTypeID = Integer.parseInt(renderRequest.getParameter("appointmentTypeID"));
        }

        renderRequest.setAttribute("criteria", new Criteria().list(appointmentTypeID));
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
}
