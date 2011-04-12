/**
 * This class is a hibernate class that is called by the Actions class methods and
 * receives POJOs. It performs business logic with POJOs and uses Hibernate to save
 * them back to database if appropriate.
 */
package edu.osu.cws.pass.util;

import edu.osu.cws.pass.models.CriterionArea;
import edu.osu.cws.pass.models.CriterionDetail;
import org.hibernate.Session;
import edu.osu.cws.pass.util.*;

import java.util.List;

public class Criteria {

    /**
     * The default appointment type to use when displaying criteria information.
     */
    public static final int DEFAULT_APPOINTMENT_TYPE = 1;

    /**
     * Takes the CriterionArea and CriterionDetail POJO objects, performs validation
     * and calls the respective hibernate method to save to db if passed validation.
     *
     * @param area      CriterionArea POJO
     * @param details   CriterionDetail POJO
     * @return errors   An array of error messages, but empty array on success.
     */
    public String[] add(CriterionArea area, CriterionDetail details) {
        return new String[2];
    }

    /**
     * Takes the CriterionArea and CriterionDetail POJO objects, performs validation
     * and calls the respective hibernate method to save to db if passed validation. If
     * the change is minor, either the CriterionArea or CriterionDetail POJOs are edited.
     * Otherwise if the description was changed a new CriterionDetail POJO is created, or
     * if the name was changed a new CriterionArea and CriterionDetail POJOs are created.
     *
     * @param area      CriterionArea POJO
     * @param details   CriterionDetail POJO
     * @return errors   An array of error messages, but empty array on success.
     */
    public String[] edit(CriterionArea area, CriterionDetail details) {
        return new String[2];
    }

    /**
     * Takes an employeeTypeID and uses hibernate to fetch the CriterionArea POJOs. If
     * there are none, it returns an empty CriterionArea array.
     *
     * @param employeeTypeID
     * @return criterias        Array of CriterionAreas
     */
    public List list(long employeeTypeID) {
        Session hsession = null;

        hsession = HibernateUtil.getSessionFactory().getCurrentSession();
        hsession.beginTransaction();
        List result = hsession.createQuery("from edu.osu.cws.pass.models.CriterionArea").list();
        hsession.getTransaction().commit();
        return result;
    }

    /**
     * Takes an ID of the CriterionArea the user is trying to delete. If successful, an empty
     * array is returned, otherwise, the array will contain error messages.
     *
     * @param ID
     * @return errors   An array of errors if there was a problem trying to delete the CriterionArea
     */
    public String[] delete(long ID) {
        return new String[2];
    }

    /**
     * Takes a string specifying the new order the criterias. Fetches all the CriterionArea for the
     * given employee type. Then it sets the new sequence in each one of them and saves the POJOs.
     *
     * @param order     The new order of criterias
     * @return errors
     */
    public String[] updateSequence(String order, long employeeTypeID) {
        return new String[2];
    }

}
