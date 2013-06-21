/**
 * This class is a hibernate class that is called by the ActionHelper class methods and
 * receives POJOs. It performs business logic with POJOs and uses Hibernate to save
 * them back to database if appropriate.
 */
package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.AppointmentType;
import edu.osu.cws.evals.models.CriterionArea;
import edu.osu.cws.evals.models.Employee;
import edu.osu.cws.evals.models.ModelException;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.Session;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class CriteriaMgr {

    /**
     * The default appointment type to use when displaying criteria information.
     */
    public static final String DEFAULT_APPOINTMENT_TYPE = AppointmentType.CLASSIFIED;

    /**
     * Takes the CriterionArea and CriterionDetail POJO objects, performs validation
     * and calls the respective hibernate method to save to db if passed validation.
     *
     * @param area      CriterionArea POJO
     * @param creator      Username of logged in user
     * @return errors   An array of error messages, but empty array on success.
     * @throws Exception
     */
    public static boolean add(CriterionArea area, Employee creator)
            throws Exception {

        area.setCreator(creator);
        area.setCreateDate(new Date());

        // validate both objects individually and then check for errors
        area.validate();

        if (area.getErrors().size() > 0) {
            return false;
        }

        Session session = HibernateUtil.getCurrentSession();
        session.save(area);

        return true;
    }

    /**
     * Handles the editing of an evaluation criteria. Depending on what changed area, description
     * or both there's different logic. It also handles propagating the changes to open assessments.
     * @param request
     * @param id
     * @param loggedInUser
     * @return
     * @throws Exception
     */
    public static boolean edit(Map<String, String[]> request, int id, Employee loggedInUser)
            throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        CriterionArea newCriterion = new CriterionArea();

        String description = request.get("description")[0];
        String name = request.get("name")[0];
        boolean propagateEdit = false;
        if (request.get("propagateEdit") != null) {
            propagateEdit = request.get("propagateEdit")[0] != null;
        }

        CriterionArea criterion = CriteriaMgr.get(id);

        boolean nameChanged = false;
        boolean descriptionChanged = false;

        String criterionDescription = criterion.getDescription();
        int descHash = criterionDescription.hashCode();
        int updatedDescHash = description.hashCode();

        if (!criterion.getName().equals(name)) {
            nameChanged = true;
        }
        if (descHash != updatedDescHash) {
            descriptionChanged = true;
        }
        if (!nameChanged && !descriptionChanged) {
            return true;
        } else {
            // copy all the values from the old CriterionArea
            CriteriaMgr.copyCriterion(loggedInUser, newCriterion, criterion);
            newCriterion.setName(name);
            newCriterion.setDescription(description);

            // validate both new area + description
            newCriterion.validate();

            // set old criteria as deleted
            CriteriaMgr.setCriteriaDeleteProperties(loggedInUser, criterion);

            // save pojos
            session.save(newCriterion);
            session.update(criterion);
        }
        if (propagateEdit) {
            //@todo: after AssessmentCriteria piece is done
        }
        //@todo: ajax
        return true;
    }

    /**
     * Sets the deletedDate and deleter properties in the CriterionArea pojo. This is used by edit and
     * delete.
     *
     * @param loggedInUser
     * @param criterion
     */
    private static void setCriteriaDeleteProperties(Employee loggedInUser, CriterionArea criterion) {
        criterion.setDeleteDate(new Date());
        criterion.setDeleter(loggedInUser);
    }

    /**
     * Copies the data from criterion into newCriterion.
     *
     * @param loggedInUser
     * @param newCriterion
     * @param criterion
     */
    private static void copyCriterion(Employee loggedInUser, CriterionArea newCriterion, CriterionArea criterion) {
        newCriterion.setName(criterion.getName());
        newCriterion.setCreator(loggedInUser);
        newCriterion.setAppointmentType(criterion.getAppointmentType());
        newCriterion.setCreateDate(new Date());
        newCriterion.setAncestorID(criterion);
        newCriterion.setDescription(criterion.getDescription());
    }

    /**
     * Takes an appointmentType, gets a Hibernate session object fetch a list of criteria.
     *
     * @param appointmentType
     * @return criteria        List of CriterionAreas
     * @throws edu.osu.cws.evals.models.ModelException
     */
    public static List<CriterionArea> list(String appointmentType) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        List results = session.createQuery("from edu.osu.cws.evals.models.CriterionArea where " +
                "appointmentType = :appointmentType AND deleteDate IS NULL ORDER BY appointmentType, name")
                .setString("appointmentType", appointmentType)
                .list();
        return results;
    }

    /**
     * Takes an ID of the CriterionArea the user is trying to delete. If successful, an empty
     * array is returned, otherwise, the array will contain error messages.
     *
     * @param id
     * @param deleter   Admin User deleting criteria
     * @throws Exception
     * @return
     */
    public static boolean delete(int id, Employee deleter) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        CriterionArea  criterion = CriteriaMgr.get(id);

        // check that the criteria is valid
        if (criterion == null || criterion.getDeleteDate() != null) {
            throw new ModelException("Invalid Evaluation Criteria");
        }

        // delete criteria and save it back
        setCriteriaDeleteProperties(deleter, criterion);

        session.update(criterion);

        return true;
    }

    /**
     * Retrieves a CriterionArea object from the db
     *
     * @param id
     * @return
     * @throws Exception
     */
    public static CriterionArea get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return (CriterionArea) session.get(CriterionArea.class, id);
    }
}
