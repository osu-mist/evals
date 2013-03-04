/**
 * This class is a hibernate class that is called by the ActionHelper class methods and
 * receives POJOs. It performs business logic with POJOs and uses Hibernate to save
 * them back to database if appropriate.
 */
package edu.osu.cws.evals.hibernate;

import edu.osu.cws.evals.models.*;
import edu.osu.cws.evals.util.HibernateUtil;
import org.hibernate.*;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CriteriaMgr {

    private EmployeeMgr employeeMgr = new EmployeeMgr();

    /**
     * The default appointment type to use when displaying criteria information.
     */
    public static final String DEFAULT_APPOINTMENT_TYPE = AppointmentType.CLASSIFIED;

    /**
     * Takes the CriterionArea and CriterionDetail POJO objects, performs validation
     * and calls the respective hibernate method to save to db if passed validation.
     *
     * @param area      CriterionArea POJO
     * @param details   CriterionDetail POJO
     * @param creator      Username of logged in user
     * @return errors   An array of error messages, but empty array on success.
     * @throws Exception
     */
    public boolean add(CriterionArea area, CriterionDetail details, Employee creator)
            throws Exception {

        area.setCreator(creator);
        area.setCreateDate(new Date());
        details.setCreator(creator);
        details.setCreateDate(new Date());

        // validate both objects individually and then check for errors
        area.validate();
        details.validate();

        if (area.getErrors().size() > 0 || details.getErrors().size() > 0) {
            return false;
        }

        Session session = HibernateUtil.getCurrentSession();
        session.save(area);
        area.addDetails(details);
        session.save(details);

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
    public boolean edit(Map<String, String[]> request, int id, Employee loggedInUser) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        CriterionArea newCriterion = new CriterionArea();
        CriterionDetail newDetails = new CriterionDetail();
        CriterionArea criterion;

        String description = request.get("description")[0];
        String name = request.get("name")[0];
        boolean propagateEdit = false;
        if (request.get("propagateEdit") != null) {
            propagateEdit = request.get("propagateEdit")[0] != null;
        }

        criterion = get(id, session);

        boolean areaChanged = false;
        boolean descriptionChanged = false;
        CriterionDetail currentDetail = criterion.getCurrentDetail();
        String criterionDescription = currentDetail.getDescription();
        int descHash = criterionDescription.hashCode();

        int updatedDescHash = description.hashCode();

        if (!criterion.getName().equals(name)) {
            areaChanged = true;
        }
        if (descHash != updatedDescHash) {
            descriptionChanged = true;
        }
        if (!areaChanged && !descriptionChanged) {
            return true;
        }

        if (areaChanged && !descriptionChanged) {
            // copy all the values from the old CriterionArea
            copyCriterion(loggedInUser, newCriterion, criterion);
            newCriterion.setName(name);

            // copy all the values form the old CriterionDetail
            copyDetails(loggedInUser, newDetails, criterion, criterionDescription);

            // validate both new area + description
            newCriterion.validate();
            newDetails.validate();

            // set old criteria as deleted
            setCriteriaDeleteProperties(loggedInUser, criterion);

            // save pojos
            session.save(newCriterion);
            session.update(criterion);
            newCriterion.addDetails(newDetails);
            session.save(newDetails);
        } else if (!areaChanged && descriptionChanged) {
            // copy all the values form the old CriterionDetail
            copyDetails(loggedInUser, newDetails, criterion, description);

            // validate both new area + description
            newDetails.validate();

            // save pojo
            criterion.addDetails(newDetails);
            session.save(newDetails);
        } else if (areaChanged && descriptionChanged) {
            // copy all the values from the old CriterionArea
            copyCriterion(loggedInUser, newCriterion, criterion);
            newCriterion.setName(name);

            // copy all the values form the old CriterionDetail
            copyDetails(loggedInUser, newDetails, criterion, description);

            // validate both new area + description
            newCriterion.validate();
            newDetails.validate();

            // set old criteria as deleted
            setCriteriaDeleteProperties(loggedInUser, criterion);

            // save pojos
            session.save(newCriterion);
            session.update(criterion);
            newCriterion.addDetails(newDetails);
            session.save(newDetails);
        }
        if (propagateEdit) {
            String sqlUpdate = "UPDATE assessments a SET a.CRITERION_DETAIL_ID = :newDetail " +
                    "WHERE a.CRITERION_DETAIL_ID = :oldDetail AND a.APPRAISAL_ID in ( " +
                    "SELECT ID FROM appraisals WHERE STATUS not in ('completed', 'closed', 'archived')" +
                    ")";
            session.createSQLQuery(sqlUpdate)
                    .setInteger("newDetail", newDetails.getId())
                    .setInteger("oldDetail", currentDetail.getId())
                    .executeUpdate();
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
    private void setCriteriaDeleteProperties(Employee loggedInUser, CriterionArea criterion) {
        criterion.setDeleteDate(new Date());
        criterion.setDeleter(loggedInUser);
    }

    /**
     * Sets up the various values in newDetails using the rest of the parameters to create a copy of the
     * criteria details.
     *
     * @param loggedInUser
     * @param newDetails
     * @param criterion
     * @param criterionDescription
     */
    private void copyDetails(Employee loggedInUser, CriterionDetail newDetails, CriterionArea criterion, String criterionDescription) {
        newDetails.setDescription(criterionDescription);
        newDetails.setAreaID(criterion);
        newDetails.setCreateDate(new Date());
        newDetails.setCreator(loggedInUser);
    }

    /**
     * Copies the data from criterion into newCriterion.
     *
     * @param loggedInUser
     * @param newCriterion
     * @param criterion
     */
    private void copyCriterion(Employee loggedInUser, CriterionArea newCriterion, CriterionArea criterion) {
        newCriterion.setName(criterion.getName());
        newCriterion.setCreator(loggedInUser);
        newCriterion.setAppointmentType(criterion.getAppointmentType());
        newCriterion.setCreateDate(new Date());
        newCriterion.setOriginalID(criterion);
    }

    /**
     * Takes an appointmentType, gets a Hibernate session object and calls a private method
     * to call the private method that just uses Hibernate to fetch the list of criteria.
     *
     * @param appointmentType
     * @return criteria        List of CriterionAreas
     * @throws edu.osu.cws.evals.models.ModelException
     */
    public List<CriterionArea> list(String appointmentType) throws ModelException, Exception {
        Session session = HibernateUtil.getCurrentSession();
        return this.list(appointmentType, session);
    }

    /**
     * Takes an appointmentType, a Hibernate session and uses Hibernate to fetch the list of
     * CriterionArea that are not deleted.
     *
     * @param appointmentType
     * @param session
     * @return criteria     List of CriterionAreas
     * @throws Exception
     */
    private List<CriterionArea> list(String appointmentType, Session session) throws Exception {
        List result = session.createQuery("from edu.osu.cws.evals.models.CriterionArea where " +
                "appointmentType = :appointmentType AND deleteDate IS NULL ORDER BY name")
                .setString("appointmentType", appointmentType)
                .list();
        return result;

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
    public boolean delete(int id, Employee deleter) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        CriterionArea criterion;
        criterion = get(id, session);

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
    public CriterionArea get(int id) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        return get(id, session);
    }

    /**
     * Retrieves a CriterionArea object from the db
     *
     * @param id
     * @param session
     * @return
     */
    private CriterionArea get(int id, Session session) {
        return (CriterionArea) session.get(CriterionArea.class, id);
    }
}
