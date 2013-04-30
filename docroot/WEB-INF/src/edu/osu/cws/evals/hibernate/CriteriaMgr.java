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
    public static boolean add(CriterionArea area, CriterionDetail details, Employee creator)
            throws Exception {
        int sequence = CriteriaMgr.getNextSequence(area.getAppointmentType());

        area.setCreator(creator);
        area.setSequence(sequence);
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
    public static boolean edit(Map<String, String[]> request, int id, Employee loggedInUser) throws Exception {
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

        criterion = CriteriaMgr.get(id);

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
            CriteriaMgr.copyCriterion(loggedInUser, newCriterion, criterion);
            newCriterion.setName(name);

            // copy all the values form the old CriterionDetail
            CriteriaMgr.copyDetails(loggedInUser, newDetails, criterion, criterionDescription);

            // validate both new area + description
            newCriterion.validate();
            newDetails.validate();

            // set old criteria as deleted
            CriteriaMgr.setCriteriaDeleteProperties(loggedInUser, criterion);

            // save pojos
            session.save(newCriterion);
            session.update(criterion);
            newCriterion.addDetails(newDetails);
            session.save(newDetails);
        } else if (!areaChanged && descriptionChanged) {
            // copy all the values form the old CriterionDetail
            CriteriaMgr.copyDetails(loggedInUser, newDetails, criterion, description);

            // validate both new area + description
            newDetails.validate();

            // save pojo
            criterion.addDetails(newDetails);
            session.save(newDetails);
        } else if (areaChanged && descriptionChanged) {
            // copy all the values from the old CriterionArea
            CriteriaMgr.copyCriterion(loggedInUser, newCriterion, criterion);
            newCriterion.setName(name);

            // copy all the values form the old CriterionDetail
            CriteriaMgr.copyDetails(loggedInUser, newDetails, criterion, description);

            // validate both new area + description
            newCriterion.validate();
            newDetails.validate();

            // set old criteria as deleted
            CriteriaMgr.setCriteriaDeleteProperties(loggedInUser, criterion);

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
    private static void setCriteriaDeleteProperties(Employee loggedInUser, CriterionArea criterion) {
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
    private static void copyDetails(Employee loggedInUser, CriterionDetail newDetails, CriterionArea criterion, String criterionDescription) {
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
    private static void copyCriterion(Employee loggedInUser, CriterionArea newCriterion, CriterionArea criterion) {
        newCriterion.setName(criterion.getName());
        newCriterion.setSequence(criterion.getSequence());
        newCriterion.setCreator(loggedInUser);
        newCriterion.setAppointmentType(criterion.getAppointmentType());
        newCriterion.setCreateDate(new Date());
        newCriterion.setOriginalID(criterion);
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
    public static boolean delete(int id, Employee deleter) throws Exception {
        Session session = HibernateUtil.getCurrentSession();
        CriterionArea criterion;
        criterion = CriteriaMgr.get(id);

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
     * Takes care of updating the sequence of the evaluation criteria.
     *
     * @param id
     * @param newPosition
     * @return
     * @throws Exception
     */
    public static boolean updateSequence(int id, int newPosition) throws Exception {
        CriterionArea criterion = get(id);

        if (criterion == null || criterion.getDeleteDate() != null) {
            throw new ModelException("Can't update sequence. Invalid Evaluation Criteria");
        }

        int originalPosition = criterion.getSequence();
        if (originalPosition == newPosition)  {
            return true;
        }

        String appointmentType = criterion.getAppointmentType();
        // whether or not we are moving the Criteria up visually
        boolean moveCriteriaToSmallerSequence = true;

        // Calculate the inclusive lower and upper bound of the objects whose sequence need to be updated
        int lowerBound = newPosition;
        int upperBound = originalPosition;
        if (originalPosition < newPosition) {
            lowerBound = originalPosition;
            upperBound = newPosition;
            moveCriteriaToSmallerSequence = false;
        }

        Session session = HibernateUtil.getCurrentSession();
        updateSequence(newPosition, originalPosition, appointmentType, lowerBound, upperBound,
                moveCriteriaToSmallerSequence, session);
        return true;
    }

    /**
     * Takes care of updating the sequence of all the affected evaluation criteria.
     *
     * @param newPosition
     * @param originalPosition
     * @param appointmentType
     * @param lowerBound
     * @param upperBound
     * @param moveCriteriaToSmallerSequence
     * @param session
     */
    private static void updateSequence(int newPosition, int originalPosition, String appointmentType,
                                int lowerBound, int upperBound,
                                boolean moveCriteriaToSmallerSequence, Session session) {
        String query = "from edu.osu.cws.evals.models.CriterionArea where deleteDate is null " +
                "and appointmentType = :appointmentType and sequence >= :lowerBound and " +
                "sequence <= :upperBound";

        List<CriterionArea> results = (List<CriterionArea>) session.createQuery(query)
                .setString("appointmentType", appointmentType)
                .setInteger("lowerBound", lowerBound)
                .setInteger("upperBound", upperBound)
                .list();
        for (CriterionArea criteria : results) {
            if (criteria.getSequence() == originalPosition) {
                criteria.setSequence(newPosition);
            } else {
                if (moveCriteriaToSmallerSequence) {
                    criteria.setSequence(criteria.getSequence() + 1);
                } else {
                    criteria.setSequence(criteria.getSequence() - 1);
                }
            }
            session.update(criteria);
        }
    }

    /**
     * Figures out the next available sequence for a CriterionArea of a specific appointment type.
     * The next available sequence is usually is size of criteria list + 1.
     * @param appointmentType
     * @return
     */
    public static int getNextSequence(String appointmentType) throws Exception {
        int availableSequence = 0;
        Session hsession = HibernateUtil.getCurrentSession();
        Query countQry = hsession.createQuery("select count(*) from edu.osu.cws.evals.models.CriterionArea " +
                "where appointmentType = :appointmentType AND deleteDate IS NULL");

        countQry.setString("appointmentType", appointmentType);
        countQry.setMaxResults(1);
        Iterator results = countQry.list().iterator();

        if (results.hasNext()) {
            availableSequence =  Integer.parseInt(results.next().toString());
        }

        return ++availableSequence;

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
